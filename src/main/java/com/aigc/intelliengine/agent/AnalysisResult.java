package com.aigc.intelliengine.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalysisResult {
    private Long frameId;
    private int frameNumber;
    private String originalPrompt;
    private String analyzedPrompt;
    private String[] suggestedTags;
    private double confidence;
    private String model;
    private long analysisTime;
}
