package com.aigc.intelliengine.agent.rag;

import com.aigc.intelliengine.agent.LlmConfig;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * OpenAI Embedding + LangChain4j InMemoryEmbeddingStore — 真正的语义向量检索.
 * 当 llm.enabled=true 时激活，替换 PromptEmbeddingService 的关键词方案.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "llm.enabled", havingValue = "true")
public class OpenAiEmbeddingService {

    private final LlmConfig llmConfig;
    private EmbeddingModel embeddingModel;
    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private final Set<Long> removedIds = new HashSet<>();

    private static final String EMBEDDING_MODEL = "text-embedding-3-small";

    @PostConstruct
    public void init() {
        if (llmConfig.getApiKey() == null || llmConfig.getApiKey().isBlank()
                || llmConfig.getApiKey().startsWith("sk-your")) {
            log.warn("LLM API key not configured, OpenAI Embedding disabled — falling back to keyword vectors");
            return;
        }
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(llmConfig.getApiKey())
                .baseUrl(llmConfig.getBaseUrl())
                .modelName(EMBEDDING_MODEL)
                .timeout(Duration.ofSeconds(15))
                .build();
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        log.info("OpenAI Embedding initialized: model={}", EMBEDDING_MODEL);
    }

    public void index(Long promptId, String text) {
        if (embeddingModel == null || embeddingStore == null) return;
        try {
            Embedding emb = embeddingModel.embed(text).content();
            Metadata metadata = new Metadata();
            metadata.put("promptId", String.valueOf(promptId));
            TextSegment segment = TextSegment.from(text, metadata);
            embeddingStore.add(emb, segment);
        } catch (Exception e) {
            log.warn("Failed to embed prompt #{}: {}", promptId, e.getMessage());
        }
    }

    public void remove(Long promptId) {
        removedIds.add(promptId);
    }

    public void clear() {
        if (embeddingStore != null) {
            embeddingStore = new InMemoryEmbeddingStore<>();
        }
    }

    public List<PromptEmbeddingService.ScoredPrompt> search(String queryText, Long selfId, int topK) {
        if (embeddingModel == null || embeddingStore == null) return Collections.emptyList();
        try {
            Embedding queryEmb = embeddingModel.embed(queryText).content();
            var results = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryEmb)
                            .maxResults(topK + 1)
                            .minScore(0.5)
                            .build());
            return results.matches().stream()
                    .map(m -> {
                        long pid = Long.parseLong(m.embedded().metadata().getString("promptId"));
                        return new PromptEmbeddingService.ScoredPrompt(pid, m.score());
                    })
                    .filter(s -> s.promptId() != selfId && !removedIds.contains(s.promptId()))
                    .limit(topK)
                    .toList();
        } catch (Exception e) {
            log.warn("Semantic search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
