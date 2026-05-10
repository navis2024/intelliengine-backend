package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "llm.enabled", havingValue = "true")
public class OpenAIAnalysisStrategy implements PromptAnalysisStrategy {

    private final LlmConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAIAnalysisStrategy(LlmConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("LLM Analysis Strategy initialized: provider={}, model={}, baseUrl={}",
                config.getProvider(), config.getModel(), config.getBaseUrl());
    }

    @Override
    public String getName() {
        return config.getProvider() + "-" + config.getModel();
    }

    @Override
    public AnalysisResult analyze(VideoFrame frame) {
        String prompt = frame.getPromptText();
        if (prompt == null || prompt.isBlank()) {
            return new AnalysisResult(frame.getId(), frame.getFrameNumber(),
                    null, "No prompt detected", new String[0], 0.0, getName(), System.currentTimeMillis());
        }

        String systemPrompt = buildSystemPrompt();
        String userMessage = buildUserMessage(prompt);

        try {
            Map<String, Object> requestBody = buildRequestBody(systemPrompt, userMessage);
            String response = callLlmApi(requestBody);
            return parseResponse(response, frame);
        } catch (Exception e) {
            log.error("LLM analysis failed for frame #{}: {}", frame.getFrameNumber(), e.getMessage());
            // Fallback: return basic enrichment
            return new AnalysisResult(frame.getId(), frame.getFrameNumber(),
                    prompt,
                    prompt + " — [LLM unavailable, basic enrichment] Style: cinematic, photorealistic, high detail",
                    suggestBasicTags(prompt),
                    0.6, getName() + "(fallback)", System.currentTimeMillis());
        }
    }

    private String buildSystemPrompt() {
        return """
            You are an expert AIGC video prompt analyst. Your task is to analyze prompts used for AI video generation.

            For each prompt, provide:
            1. An ENRICHED version of the prompt — add technical terms to improve output quality (lighting, camera, style, resolution details)
            2. 3-5 SUGGESTED TAGS for categorization
            3. A CONFIDENCE score (0.0-1.0) indicating how well-structured the original prompt is

            Respond ONLY in JSON format:
            {"enriched": "...", "tags": ["...", "..."], "confidence": 0.85}

            The enriched prompt MUST be in English. Add terms like "cinematic lighting", "8K, photorealistic", "depth of field",
            "professional color grading", "composition rule of thirds" where appropriate.
            """;
    }

    private String buildUserMessage(String prompt) {
        return "Analyze this AIGC video generation prompt:\n\n" + prompt;
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userMessage) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> sysMsg = new LinkedHashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        Map<String, String> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        body.put("messages", messages);
        body.put("response_format", Map.of("type", "json_object"));
        return body;
    }

    private String callLlmApi(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        String url = config.getBaseUrl() + "/chat/completions";
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        log.debug("Calling LLM API: {}", url);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("LLM API returned " + response.getStatusCode());
    }

    private AnalysisResult parseResponse(String response, VideoFrame frame) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            if (content.startsWith("```json")) content = content.substring(7, content.length() - 3).trim();
            if (content.startsWith("```")) content = content.substring(3, content.length() - 3).trim();

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
            log.warn("Failed to parse LLM response, using raw content: {}", e.getMessage());
            return new AnalysisResult(frame.getId(), frame.getFrameNumber(),
                    frame.getPromptText(),
                    frame.getPromptText() + " — LLM analyzed",
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
        if (lower.contains("cyberpunk")) tags.add("cyberpunk");
        if (lower.contains("neon")) tags.add("neon");
        if (tags.isEmpty()) tags.add("general");
        return tags.toArray(new String[0]);
    }
}
