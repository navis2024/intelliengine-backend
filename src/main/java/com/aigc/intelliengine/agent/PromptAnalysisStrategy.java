package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import java.util.concurrent.CompletableFuture;

public interface PromptAnalysisStrategy {
    String getName();
    AnalysisResult analyze(VideoFrame frame);
    default CompletableFuture<AnalysisResult> analyzeAsync(VideoFrame frame) {
        return CompletableFuture.supplyAsync(() -> analyze(frame));
    }
}
