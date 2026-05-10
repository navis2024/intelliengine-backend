package com.aigc.intelliengine.agent.collaboration;

import com.aigc.intelliengine.agent.tools.AgentTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WorkerAgent — 任务执行单元.
 *
 * 职责:
 *   1. 监听Supervisor分派的任务
 *   2. 调用对应工具执行
 *   3. 将结果回报给Supervisor
 *
 * 每个Worker对应一个工具的专业执行者.
 */
@Slf4j
@Component
public class WorkerAgent {

    private final AgentBus bus;
    private final Map<String, AgentTool> toolRegistry;
    private static final String AGENT_NAME = "WORKER";

    public WorkerAgent(AgentBus bus, java.util.List<AgentTool> tools) {
        this.bus = bus;
        this.toolRegistry = new java.util.LinkedHashMap<>();
        for (AgentTool tool : tools) {
            toolRegistry.put(tool.name(), tool);
        }
    }

    /** 异步监听Supervisor分派的任务 */
    @Async
    @EventListener
    public void onTaskAssigned(AgentBus.AgentMessageEvent event) {
        AgentMessage msg = event.message();
        if (msg.getType() != AgentMessage.AgentMessageType.TASK_ASSIGN) return;
        if (!msg.getToAgent().startsWith(AGENT_NAME)) return;

        String toolName = msg.getTask();
        AgentTool tool = toolRegistry.get(toolName);
        if (tool == null) {
            sendResponse(msg, AgentMessage.AgentMessageType.TASK_FAILED,
                    Map.of("error", "未知工具: " + toolName));
            return;
        }

        log.info("Worker received task: {}, tool={}", msg.getMessageId(), toolName);

        try {
            // 从payload中提取参数
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) msg.getPayload()
                    .getOrDefault("args", Map.of());
            if (args.isEmpty()) {
                args = inferArgs(toolName, (String) msg.getPayload().getOrDefault("userTask", ""));
            }

            String result = tool.execute(args);
            sendResponse(msg, AgentMessage.AgentMessageType.TASK_COMPLETE,
                    Map.of("result", result, "toolName", toolName));
            log.info("Worker completed: {} → {}", toolName, result.substring(0, Math.min(80, result.length())));
        } catch (Exception e) {
            log.error("Worker failed: {}", toolName, e);
            sendResponse(msg, AgentMessage.AgentMessageType.TASK_FAILED,
                    Map.of("error", e.getMessage(), "toolName", toolName));
        }
    }

    private void sendResponse(AgentMessage original, AgentMessage.AgentMessageType type, Map<String, Object> payload) {
        AgentMessage response = AgentMessage.builder()
                .fromAgent(AGENT_NAME)
                .toAgent(original.getFromAgent())
                .type(type)
                .task(original.getTask())
                .payload(payload)
                .build();
        bus.send(response);
    }

    private Map<String, Object> inferArgs(String toolName, String task) {
        Map<String, Object> args = new java.util.LinkedHashMap<>();
        if (toolName.equals("search_prompts")) args.put("query", task);
        if (toolName.equals("generate_report")) {
            args.put("title", "Agent协同分析报告");
            args.put("type", "PROMPT_QUALITY");
        }
        return args;
    }
}
