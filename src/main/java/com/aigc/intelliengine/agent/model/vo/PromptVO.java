package com.aigc.intelliengine.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Prompt信息")
public class PromptVO {
    private Long id;
    private String promptText;
    private String promptType;
    private String styleTags;
    private Long sourceVideoId;
    private Long sourceFrameId;
    private Long createdBy;
    private Integer useCount;
    private BigDecimal rating;
    private LocalDateTime createdAt;
}
