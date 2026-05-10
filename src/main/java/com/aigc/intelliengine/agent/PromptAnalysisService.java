package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PromptAnalysisService {

    private final PromptAnalysisStrategy strategy;

    public PromptAnalysisService(
            @Autowired(required = false) LangChain4jAnalysisStrategy langChain4jStrategy,
            @Autowired(required = false) OpenAIAnalysisStrategy openAiStrategy,
            MockAnalysisStrategy mockStrategy) {
        // Priority: LangChain4j > OpenAI > Mock
        if (langChain4jStrategy != null) {
            this.strategy = langChain4jStrategy;
        } else if (openAiStrategy != null) {
            this.strategy = openAiStrategy;
        } else {
            this.strategy = mockStrategy;
        }
        log.info("PromptAnalysisService active strategy: {}", strategy.getName());
    }

    public AnalysisResult analyzeFrame(VideoFrame frame) {
        log.info("Analyzing frame {} via {}", frame.getFrameNumber(), strategy.getName());
        return strategy.analyze(frame);
    }

    public CompletableFuture<AnalysisResult> analyzeFrameAsync(VideoFrame frame) {
        return strategy.analyzeAsync(frame);
    }
}
