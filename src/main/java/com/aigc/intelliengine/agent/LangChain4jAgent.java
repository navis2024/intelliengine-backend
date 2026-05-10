package com.aigc.intelliengine.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j AiServices interface — defines the Agent's conversational contract.
 * ChatMemory is automatically managed per-conversation via {@link MemoryId}.
 */
public interface LangChain4jAgent {

    @SystemMessage("""
        You are an expert AIGC video prompt analyst for the IntelliEngine platform.
        Your tasks:
        1. Analyze and enrich AIGC video generation prompts — add technical camera/lighting/style terms
        2. Suggest 3-5 categorization tags
        3. Rate prompt quality with a confidence score (0.0–1.0)
        4. When asked, query asset metadata or check FFmpeg processing status using available tools
        Always respond in JSON format: {"enriched": "...", "tags": ["...", "..."], "confidence": 0.85}
        """)
    String analyze(@MemoryId String conversationId, @UserMessage String prompt);
}
