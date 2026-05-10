package com.aigc.intelliengine.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "帧分析结果")
public class FrameAnalysisVO {
    private String frameId;
    private Integer frameNumber;
    private String originalPrompt;
    private String analyzedPrompt;
    private String[] suggestedTags;
    private Double confidence;
    private String model;
    private Long analysisTime;
}
