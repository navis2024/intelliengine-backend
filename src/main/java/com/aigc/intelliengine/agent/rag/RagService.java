package com.aigc.intelliengine.agent.rag;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * RAG门面 — 自动在OpenAI Embedding和本地关键词向量之间切换.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final PromptEmbeddingService keywordService;

    @Autowired(required = false)
    private OpenAiEmbeddingService openAiService;

    @PostConstruct
    public void init() {
        if (openAiService != null) {
            log.info("RagService: using OpenAI Embedding mode");
        } else {
            log.info("RagService: using keyword-frequency vector mode (llm.enabled=false)");
        }
    }

    public void index(Long promptId, String text) {
        if (openAiService != null) {
            openAiService.index(promptId, text);
        }
        keywordService.index(promptId, text);
    }

    public void remove(Long promptId) {
        if (openAiService != null) {
            openAiService.remove(promptId);
        }
        keywordService.remove(promptId);
    }

    public void clear() {
        if (openAiService != null) {
            openAiService.clear();
        }
        keywordService.clear();
    }

    public List<PromptEmbeddingService.ScoredPrompt> search(String queryText, Long selfId, int topK) {
        if (openAiService != null) {
            List<PromptEmbeddingService.ScoredPrompt> results = openAiService.search(queryText, selfId, topK);
            if (!results.isEmpty()) return results;
        }
        return keywordService.search(queryText, selfId, topK);
    }
}
