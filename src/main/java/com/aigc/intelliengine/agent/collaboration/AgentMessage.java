package com.aigc.intelliengine.agent.collaboration;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AgentMessage {
    @Builder.Default
    private String messageId = UUID.randomUUID().toString().substring(0, 8);
    private String fromAgent;
    private String toAgent;
    private AgentMessageType type;
    private String task;
    private Map<String, Object> payload;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum AgentMessageType {
        TASK_ASSIGN, TASK_ACCEPT, TASK_COMPLETE, TASK_FAILED,
        QUERY, RESPONSE, BROADCAST, HEARTBEAT
    }
}
