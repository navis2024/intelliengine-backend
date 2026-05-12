package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface PromptAnalysisStrategy {
    String getName();
    AnalysisResult analyze(VideoFrame frame);
    default CompletableFuture<AnalysisResult> analyzeAsync(VideoFrame frame, Executor executor) {
        return CompletableFuture.supplyAsync(() -> analyze(frame), executor);
    }
}
