package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.tools.AgentTool;
import com.aigc.intelliengine.agent.model.vo.AgentStepVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent编排引擎 — ReAct(Reasoning + Acting) 模式.
 *
 * 设计思路:
 *   1. 注册所有可用工具 → AgentTool注册表
 *   2. 接收用户任务 → 解析意图 → 规划步骤
 *   3. 顺序执行: 思考(Thought) → 行动(Action) → 观察(Observation) → 下一步思考...
 *   4. 通过SSE实时推送每一步的状态
 *
 * 当前版本使用关键词匹配做任务规划(无需LLM).
 * 启用LangChain4j后可升级为LLM驱动的完全自主决策.
 */
@Slf4j
@Service
public class AgentOrchestrator {

    private final List<AgentTool> tools;
    private final Map<String, AgentTool> toolRegistry = new LinkedHashMap<>();

    public AgentOrchestrator(List<AgentTool> tools) {
        this.tools = tools;
    }

    @PostConstruct
    public void init() {
        for (AgentTool tool : tools) {
            toolRegistry.put(tool.name(), tool);
        }
        log.info("AgentOrchestrator initialized with {} tools: {}", tools.size(),
                tools.stream().map(AgentTool::name).collect(Collectors.joining(", ")));
    }

    /**
     * 执行Agent任务 — 流式输出每一步
     */
    public SseEmitter execute(String taskDescription, Long userId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5min timeout

        new Thread(() -> {
            List<AgentStepVO> steps = new ArrayList<>();
            try {
                // Phase 1: 任务解析
                emitStep(emitter, "planning", "解析任务: " + taskDescription, null);
                List<String> plan = planSteps(taskDescription);
                emitStep(emitter, "planned", "规划了 " + plan.size() + " 个步骤: " + String.join(" → ", plan), null);

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
                emitStep(emitter, "summary", buildSummary(steps, context.toString()), null);
                emitter.complete();
                log.info("Agent task completed: {} steps", plan.size());

            } catch (Exception e) {
                log.error("Agent execution failed", e);
                try { emitStep(emitter, "error", "Agent执行异常: " + e.getMessage(), null); } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        }, "agent-worker-" + System.currentTimeMillis() % 10000).start();

        return emitter;
    }

    /** 基于关键词匹配做任务规划 */
    private List<String> planSteps(String task) {
        List<String> plan = new ArrayList<>();
        String lower = task.toLowerCase();

        // 视频相关任务: 先抽帧再分析
        if (lower.contains("视频") || lower.contains("video") || lower.contains("帧")) {
            if (lower.contains("分析") || lower.contains("analyze")) {
                plan.add("extract_frames");
                plan.add("analyze_prompt");
            } else if (lower.contains("抽取") || lower.contains("extract")) {
                plan.add("extract_frames");
            }
        }

        // Prompt相关
        if (lower.contains("prompt") || lower.contains("提示词")) {
            if (lower.contains("分析") || lower.contains("优化") || lower.contains("analyze")) {
                plan.add("search_prompts");
                plan.add("analyze_prompt");
            } else if (lower.contains("搜索") || lower.contains("搜索") || lower.contains("查找")) {
                plan.add("search_prompts");
            }
        }

        // 报告
        if (lower.contains("报告") || lower.contains("report") || lower.contains("汇总")) {
            plan.add("generate_report");
        }

        // 如果没有匹配到任何工具，默认搜索prompt + 生成报告
        if (plan.isEmpty()) {
            plan.add("search_prompts");
            plan.add("generate_report");
        }

        return plan;
    }

    private String generateThought(String toolName, String context, int stepNum, int total) {
        AgentTool tool = toolRegistry.get(toolName);
        String desc = tool != null ? tool.description() : "执行操作";
        return String.format("[步骤%d/%d] %s — %s", stepNum + 1, total, toolName, desc.split("。")[0]);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> inferArgs(String toolName, String task, String context) {
        Map<String, Object> args = new LinkedHashMap<>();
        // 从context中提取可能的ID
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

        // 默认参数
        if (toolName.equals("analyze_prompt")) args.putIfAbsent("frameId", 1L);
        if (toolName.equals("extract_frames")) args.putIfAbsent("videoId", 1L);
        if (toolName.equals("search_prompts")) args.putIfAbsent("query", task);
        if (toolName.equals("generate_report")) {
            args.putIfAbsent("title", "Agent自动分析报告");
            args.putIfAbsent("type", "PROMPT_QUALITY");
        }

        return args;
    }

    private String buildSummary(List<AgentStepVO> steps, String context) {
        return String.format("Agent任务执行完成。共%d个步骤。结果摘要:\n%s", steps.size(), context.trim());
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
