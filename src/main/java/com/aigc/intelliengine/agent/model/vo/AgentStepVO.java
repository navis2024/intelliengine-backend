package com.aigc.intelliengine.agent.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AgentStepVO {
    private String phase;
    private String message;
    private String timestamp;
    private Map<String, Object> extra;
}
