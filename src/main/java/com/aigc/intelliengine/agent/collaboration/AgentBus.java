package com.aigc.intelliengine.agent.collaboration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Agent通信总线 — 基于Spring Event的轻量级 Agent-to-Agent 消息传递.
 * Supervisor分配任务 → Worker接收执行 → 结果回传 → Supervisor汇总.
 */
@Slf4j
@Component
public class AgentBus {

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, List<AgentMessage>> inboxes = new ConcurrentHashMap<>();
    private final Map<String, List<AgentMessage>> sentMessages = new ConcurrentHashMap<>();

    public AgentBus(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /** 发送消息 — 同步投递到目标Agent的收件箱 + Spring Event广播 */
    public void send(AgentMessage message) {
        // 存入收件箱
        inboxes.computeIfAbsent(message.getToAgent(), k -> new CopyOnWriteArrayList<>()).add(message);
        sentMessages.computeIfAbsent(message.getFromAgent(), k -> new CopyOnWriteArrayList<>()).add(message);
        // 广播事件，允许其他Agent对消息做出反应
        eventPublisher.publishEvent(new AgentMessageEvent(this, message));
        log.debug("[AgentBus] {} → {} [{}]: {}", message.getFromAgent(),
                message.getToAgent(), message.getType(), message.getTask());
    }

    /** 获取Agent收件箱未读消息 */
    public List<AgentMessage> pollInbox(String agentName) {
        return List.copyOf(inboxes.getOrDefault(agentName, Collections.emptyList()));
    }

    /** 获取消息历史 */
    public List<AgentMessage> getHistory(String agentName) {
        return List.copyOf(sentMessages.getOrDefault(agentName, Collections.emptyList()));
    }

    /** 清空Agent收件箱 */
    public void clearInbox(String agentName) {
        inboxes.remove(agentName);
    }

    // ==================== Spring Event Bridge ====================

    /**
     * Agent消息事件 — 被Spring ApplicationContext广播.
     * 其他Agent通过@EventListener异步监听，实现响应式协作.
     */
    public record AgentMessageEvent(Object source, AgentMessage message) {}

    @Async
    @EventListener
    public void onAgentMessage(AgentMessageEvent event) {
        AgentMessage msg = event.message();
        // Auditor监听所有消息做合规检查
        if (!"AUDITOR".equals(msg.getToAgent()) && !"AUDITOR".equals(msg.getFromAgent())) {
            log.debug("[AgentBus.Event] {} → {} — {}", msg.getFromAgent(), msg.getToAgent(), msg.getType());
        }
    }
}
