package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockAnalysisStrategy implements PromptAnalysisStrategy {

    @Override public String getName() { return "mock-llm-v2"; }

    @Override
    public AnalysisResult analyze(VideoFrame frame) {
        log.info("Mock analyzing frame #{} of video {}", frame.getFrameNumber(), frame.getVideoId());
        String original = frame.getPromptText();
        String enriched = (original == null || original.isBlank())
            ? "未检测到提示词"
            : original + " — Style: cinematic, photorealistic, professional lighting, high detail";

        return new AnalysisResult(
            frame.getId(), frame.getFrameNumber(), original, enriched,
            suggestTags(enriched), 0.85, getName(), System.currentTimeMillis()
        );
    }

    private String[] suggestTags(String prompt) {
        if (prompt.contains("cinematic")) return new String[]{"cinematic", "film", "professional"};
        if (prompt.contains("anime"))     return new String[]{"anime", "animation", "stylized"};
        return new String[]{"general", "photorealistic"};
    }
}
