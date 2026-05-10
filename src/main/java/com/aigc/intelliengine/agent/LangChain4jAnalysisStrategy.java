package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * LangChain4j-powered analysis strategy — supports ChatMemory, Function Calling (@Tool),
 * and structured JSON output via AiServices.
 *
 * Activated when: langchain4j.enabled=true
 * Falls back to: OpenAIAnalysisStrategy (llm.enabled=true) or MockAnalysisStrategy
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "langchain4j.enabled", havingValue = "true")
public class LangChain4jAnalysisStrategy implements PromptAnalysisStrategy {

    private final LangChain4jAgent agent;
    private final LangChain4jConfig config;
    private final ObjectMapper objectMapper;

    public LangChain4jAnalysisStrategy(LangChain4jAgent agent, LangChain4jConfig config) {
        this.agent = agent;
        this.config = config;
        this.objectMapper = new ObjectMapper();
        log.info("LangChain4j Analysis Strategy initialized: model={}, baseUrl={}, tools=enabled",
                config.getModelName(), config.getBaseUrl());
    }

    @Override
    public String getName() {
        return "langchain4j-" + config.getModelName();
    }

    @Override
    public AnalysisResult analyze(VideoFrame frame) {
        String prompt = frame.getPromptText();
        if (prompt == null || prompt.isBlank()) {
            return new AnalysisResult(frame.getId(), frame.getFrameNumber(),
                    null, "No prompt detected", new String[0], 0.0, getName(), System.currentTimeMillis());
        }

        try {
            // Conversation ID ties memory to this specific asset for context
            String conversationId = "frame-analysis-" + frame.getVideoId();
            String response = agent.analyze(conversationId, prompt);
            return parseResponse(response, frame);
        } catch (Exception e) {
            log.error("LangChain4j analysis failed for frame #{}: {}", frame.getFrameNumber(), e.getMessage());
            return new AnalysisResult(frame.getId(), frame.getFrameNumber(),
                    frame.getPromptText(),
                    frame.getPromptText() + " — [Agent unavailable, basic enrichment]",
                    suggestBasicTags(frame.getPromptText()),
                    0.5, getName() + "(fallback)", System.currentTimeMillis());
        }
    }

    private AnalysisResult parseResponse(String content, VideoFrame frame) {
        try {
            // Strip markdown code fences if present
            if (content.startsWith("```json")) content = content.substring(7, content.length() - 3).trim();
            else if (content.startsWith("```")) content = content.substring(3, content.length() - 3).trim();

            JsonNode analysis = objectMapper.readTree(content);
            String enriched = analysis.path("enriched").asText(frame.getPromptText());
            List<String> tags = new ArrayList<>();
            analysis.path("tags").forEach(t -> tags.add(t.asText()));
            double confidence = analysis.path("confidence").asDouble(0.8);

            return new AnalysisResult(frame.getId(), frame.getFrameNumber(),
                    frame.getPromptText(), enriched,
                    tags.toArray(new String[0]), confidence,
                    getName(), System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("Failed to parse LangChain4j response, using raw content");
            return new AnalysisResult(frame.getId(), frame.getFrameNumber(),
                    frame.getPromptText(), content,
                    suggestBasicTags(frame.getPromptText()), 0.7,
                    getName(), System.currentTimeMillis());
        }
    }

    private String[] suggestBasicTags(String prompt) {
        List<String> tags = new ArrayList<>();
        String lower = prompt.toLowerCase();
        if (lower.contains("cinematic")) tags.add("cinematic");
        if (lower.contains("anime")) tags.add("anime");
        if (lower.contains("8k") || lower.contains("4k")) tags.add("high-resolution");
        if (lower.contains("photorealistic")) tags.add("photorealistic");
        if (tags.isEmpty()) tags.add("general");
        return tags.toArray(new String[0]);
    }
}
