package com.aigc.intelliengine.agent.collaboration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WorkflowEngine — 多Agent工作流编排引擎.
 *
 * 编排模式:
 *   Supervisor(规划) → Workers(执行) → Auditor(审查) → Supervisor(汇总)
 *
 * SSE流式输出整个协同过程的每一步.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final AgentBus bus;
    private final SupervisorAgent supervisor;

    /**
     * 启动多Agent协同工作流
     * 流程: 规划 → 分派 → 执行 → 收集 → 汇总
     */
    public SseEmitter launch(String userTask) {
        SseEmitter emitter = new SseEmitter(300_000L);

        new Thread(() -> {
            try {
                // Phase 1: Supervisor规划
                emit(emitter, "supervisor-planning", "Supervisor正在分析任务: " + userTask);
                List<AgentMessage> assignments = supervisor.planAndDispatch(userTask);
                emit(emitter, "supervisor-dispatched",
                        String.format("Supervisor分派了%d个子任务给Workers: %s",
                                assignments.size(),
                                assignments.stream().map(AgentMessage::getTask).collect(Collectors.joining(", "))));

                // Phase 2: 等待Worker结果
                emit(emitter, "waiting", "等待Workers执行中...");
                long deadline = System.currentTimeMillis() + 60_000;
                int completedCount = 0;

                while (System.currentTimeMillis() < deadline && completedCount < assignments.size()) {
                    List<AgentMessage> inbox = bus.pollInbox("SUPERVISOR");
                    completedCount = (int) inbox.stream()
                            .filter(m -> m.getType() == AgentMessage.AgentMessageType.TASK_COMPLETE
                                      || m.getType() == AgentMessage.AgentMessageType.TASK_FAILED)
                            .count();

                    for (AgentMessage msg : inbox) {
                        String status = msg.getType() == AgentMessage.AgentMessageType.TASK_COMPLETE
                                ? "完成" : "失败";
                        Object result = msg.getPayload() != null ? msg.getPayload().get("result") : "无";
                        emit(emitter, "worker-update",
                                String.format("[%s] %s → %s: %s", status, msg.getFromAgent(), msg.getTask(), result));
                    }

                    if (completedCount < assignments.size()) {
                        Thread.sleep(500);
                    }
                }

                // Phase 3: 审计
                emit(emitter, "auditing", "Auditor正在审查所有Worker结果...");
                List<AgentMessage> history = bus.getHistory("WORKER");
                long successCount = history.stream()
                        .filter(m -> m.getType() == AgentMessage.AgentMessageType.TASK_COMPLETE).count();
                emit(emitter, "audit-complete",
                        String.format("审计完成: %d/%d 成功", successCount, history.size()));

                // Phase 4: 汇总
                emit(emitter, "summary",
                        String.format("多Agent协同完成 — %d个Worker执行了%d个任务, 成功%d个, 失败%d个",
                                assignments.size(), history.size(), successCount, history.size() - successCount));

                emitter.complete();

            } catch (Exception e) {
                log.error("Workflow failed", e);
                try { emit(emitter, "error", "工作流异常: " + e.getMessage()); } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        }, "workflow-" + System.currentTimeMillis() % 10000).start();

        return emitter;
    }

    private void emit(SseEmitter emitter, String phase, String message) throws IOException {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("phase", phase);
        event.put("message", message);
        event.put("timestamp", LocalDateTime.now().toString());
        emitter.send(SseEmitter.event().name("workflow-step").data(event));
    }
}
