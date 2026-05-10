package com.aigc.intelliengine.agent.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SemanticPromptVO {
    private Long id;
    private String promptText;
    private String promptType;
    private String styleTags;
    private Long sourceVideoId;
    private Long sourceFrameId;
    private Integer useCount;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private Double similarityScore;
}
