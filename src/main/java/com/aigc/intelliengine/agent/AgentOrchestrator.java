package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.tools.AgentTool;
import com.aigc.intelliengine.common.metrics.MetricsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent编排引擎 — ReAct(Reasoning + Acting) 模式.
 *
 * 双模规划:
 *   1. LLM驱动 (langchain4j.enabled=true): LLM理解任务语义，自主选择工具链
 *   2. 关键词匹配 (fallback): 中文关键词规则匹配
 */
@Slf4j
@Service
public class AgentOrchestrator {

    private final List<AgentTool> tools;
    private final Map<String, AgentTool> toolRegistry = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MetricsService metrics;

    @Autowired(required = false)
    private OpenAiChatModel chatModel;

    private boolean llmPlanningAvailable;

    public AgentOrchestrator(List<AgentTool> tools, MetricsService metrics) {
        this.tools = tools;
        this.metrics = metrics;
    }

    @PostConstruct
    public void init() {
        for (AgentTool tool : tools) {
            toolRegistry.put(tool.name(), tool);
        }
        llmPlanningAvailable = chatModel != null;
        log.info("AgentOrchestrator initialized with {} tools: {}. LLM planning: {}",
                tools.size(),
                tools.stream().map(AgentTool::name).collect(Collectors.joining(", ")),
                llmPlanningAvailable ? "enabled" : "disabled (keyword fallback)");
    }

    /** 执行Agent任务 — 流式输出每一步 */
    public SseEmitter execute(String taskDescription, Long userId) {
        SseEmitter emitter = new SseEmitter(300_000L);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            String status = "success";
            try {
                // Phase 1: 任务解析
                emitStep(emitter, "planning",
                        (llmPlanningAvailable ? "[LLM驱动] " : "[关键词] ") + "解析任务: " + taskDescription, null);

                String planSource;
                List<String> plan;
                if (llmPlanningAvailable) {
                    plan = planStepsWithLLM(taskDescription);
                    planSource = "LLM";
                } else {
                    plan = planStepsByKeyword(taskDescription);
                    planSource = "关键词匹配";
                }

                emitStep(emitter, "planned",
                        String.format("[%s] 规划了%d个步骤: %s", planSource, plan.size(),
                                String.join(" → ", plan)), null);

                // Phase 2: 逐步执行
                StringBuilder context = new StringBuilder();
                for (int i = 0; i < plan.size(); i++) {
                    String action = plan.get(i);
                    AgentTool tool = toolRegistry.get(action);
                    if (tool == null) {
                        emitStep(emitter, "error", "未知工具: " + action, null);
                        continue;
                    }

                    // Thought
                    String thought = generateThought(action, context.toString(), i, plan.size());
                    emitStep(emitter, "thought", thought, null);

                    // Action
                    Map<String, Object> args = inferArgs(action, taskDescription, context.toString());
                    emitStep(emitter, "action", "调用工具 " + tool.name() + "(" + formatArgs(args) + ")", null);

                    // Observation
                    String observation;
                    try {
                        observation = tool.execute(args);
                    } catch (Exception e) {
                        observation = "工具执行异常: " + e.getMessage();
                        log.error("Tool {} failed", tool.name(), e);
                    }
                    emitStep(emitter, "observation", observation, null);

                    context.append("\n[步骤").append(i + 1).append(" ").append(tool.name())
                           .append("]: ").append(observation);
                }

                // Phase 3: 汇总
                emitStep(emitter, "summary", buildSummary(context.toString()), null);
                emitter.complete();
                long elapsed = System.currentTimeMillis() - start;
                log.info("Agent task completed: {} steps, source={}, elapsed={}ms", plan.size(), planSource, elapsed);

            } catch (Exception e) {
                status = "error";
                log.error("Agent execution failed", e);
                try { emitStep(emitter, "error", "Agent执行异常: " + e.getMessage(), null); } catch (Exception ignored) {}
                emitter.completeWithError(e);
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                metrics.recordAgentExecution(elapsed, status);
            }
        }, "agent-worker-" + System.currentTimeMillis() % 10000).start();

        return emitter;
    }

    /** LLM驱动规划 — 让LLM根据任务语义和工具描述自主生成执行计划 */
    private List<String> planStepsWithLLM(String task) {
        try {
            String toolsJson = tools.stream()
                    .map(t -> String.format("  {\"name\": \"%s\", \"desc\": \"%s\"}",
                            t.name(), t.description().replace("\"", "'")))
                    .collect(Collectors.joining(",\n"));

            String prompt = """
                你是一个任务规划器。根据用户任务和可用工具列表，生成一个执行计划。

                可用工具:
                [%s]

                规则:
                1. 只使用可用工具中的工具名
                2. 返回JSON数组格式: ["tool1", "tool2", ...]
                3. 按执行顺序排列
                4. 如果任务涉及视频分析，先抽帧(extract_frames)再分析
                5. 搜索prompt(search_prompts)通常在analyze_prompt之前
                6. 生成报告(generate_report)通常在最后

                用户任务: %s

                只返回JSON数组，不要其他文字。
                """.formatted(toolsJson, task);

            ChatRequest request = ChatRequest.builder()
                    .messages(Collections.singletonList(
                            dev.langchain4j.data.message.UserMessage.from(prompt)))
                    .responseFormat(ResponseFormat.builder()
                            .type(ResponseFormatType.JSON)
                            .build())
                    .build();

            String response = chatModel.chat(request).aiMessage().text().trim();

            // Parse JSON array
            if (response.startsWith("```")) {
                response = response.replaceAll("```json|```", "").trim();
            }

            @SuppressWarnings("unchecked")
            List<String> plan = objectMapper.readValue(response, new TypeReference<List<String>>() {});

            // Validate: only keep known tools
            List<String> valid = plan.stream()
                    .filter(toolRegistry::containsKey)
                    .collect(Collectors.toList());

            if (!valid.isEmpty()) {
                log.debug("LLM plan: {} → validated: {}", plan, valid);
                return valid;
            }

        } catch (Exception e) {
            log.warn("LLM planning failed, falling back to keyword: {}", e.getMessage());
        }

        return planStepsByKeyword(task);
    }

    /** 关键词匹配规划 (fallback) */
    private List<String> planStepsByKeyword(String task) {
        List<String> plan = new ArrayList<>();
        String lower = task.toLowerCase();

        if (lower.contains("视频") || lower.contains("video") || lower.contains("帧")) {
            if (lower.contains("分析") || lower.contains("analyze")) {
                plan.add("extract_frames");
                plan.add("analyze_prompt");
            } else if (lower.contains("抽取") || lower.contains("extract")) {
                plan.add("extract_frames");
            }
        }

        if (lower.contains("prompt") || lower.contains("提示词")) {
            if (lower.contains("分析") || lower.contains("优化") || lower.contains("analyze")) {
                plan.add("search_prompts");
                plan.add("analyze_prompt");
            } else if (lower.contains("搜索") || lower.contains("查找")) {
                plan.add("search_prompts");
            }
        }

        if (lower.contains("报告") || lower.contains("report") || lower.contains("汇总")) {
            plan.add("generate_report");
        }

        if (plan.isEmpty()) {
            plan.add("search_prompts");
            plan.add("generate_report");
        }

        return plan;
    }

    private String generateThought(String toolName, String context, int stepNum, int total) {
        AgentTool tool = toolRegistry.get(toolName);
        String desc = tool != null ? tool.description() : "执行操作";
        return String.format("[步骤%d/%d] %s — %s", stepNum + 1, total, toolName,
                desc.contains("。") ? desc.substring(0, desc.indexOf("。")) : desc);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> inferArgs(String toolName, String task, String context) {
        Map<String, Object> args = new LinkedHashMap<>();
        for (String token : task.replaceAll("[^0-9]", " ").split("\\s+")) {
            if (token.isBlank()) continue;
            try {
                long id = Long.parseLong(token);
                if (toolName.equals("extract_frames") || toolName.equals("analyze_prompt")) {
                    args.putIfAbsent("videoId", id);
                    args.putIfAbsent("frameId", id);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (toolName.equals("analyze_prompt")) args.putIfAbsent("frameId", 1L);
        if (toolName.equals("extract_frames")) args.putIfAbsent("videoId", 1L);
        if (toolName.equals("search_prompts")) args.putIfAbsent("query", task);
        if (toolName.equals("generate_report")) {
            args.putIfAbsent("title", "Agent自动分析报告");
            args.putIfAbsent("type", "PROMPT_QUALITY");
        }

        return args;
    }

    private String buildSummary(String context) {
        return String.format("Agent任务执行完成。\n执行上下文:\n%s", context.trim());
    }

    private String formatArgs(Map<String, Object> args) {
        return args.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
    }

    private void emitStep(SseEmitter emitter, String phase, String message, Map<String, Object> extra) throws IOException {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("phase", phase);
        event.put("message", message);
        event.put("timestamp", LocalDateTime.now().toString());
        if (extra != null) event.putAll(extra);
        emitter.send(SseEmitter.event().name("agent-step").data(event));
    }
}
