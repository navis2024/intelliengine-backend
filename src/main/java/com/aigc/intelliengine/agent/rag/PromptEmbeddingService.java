package com.aigc.intelliengine.agent.rag;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 轻量级Prompt向量化服务 — 使用关键词频率向量做相似度计算.
 * 当LLM API不可用时，用TF向量 + 余弦相似度实现语义检索的近似替代.
 *
 * 设计理由：在项目演示/面试中无需外部API即可展示RAG全链路能力.
 * 生产环境只需替换为OpenAiEmbeddingService即可获得真正的语义向量.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "llm.enabled", havingValue = "false", matchIfMissing = true)
public class PromptEmbeddingService {

    @Value("${rag.min-keyword-length:2}")
    private int minKeywordLength;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{Punct}\\s]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "can", "shall", "to", "of", "in", "for",
            "on", "with", "at", "by", "from", "as", "into", "through", "during",
            "before", "after", "above", "below", "between", "under", "again",
            "further", "then", "once", "here", "there", "when", "where", "why",
            "how", "all", "both", "each", "few", "more", "most", "other", "some",
            "such", "no", "nor", "not", "only", "own", "same", "so", "than",
            "too", "very", "just", "that", "this", "it", "its", "and", "but",
            "or", "if", "because", "about", "up", "out", "also", "any", "which",
            "的", "是", "了", "在", "和", "也", "就", "都", "而", "及",
            "与", "着", "或", "一", "个", "为", "要", "并", "以", "及"
    );

    // 全局词表：term → index
    private final Map<String, Integer> vocabulary = new ConcurrentHashMap<>();
    // 向量缓存：promptId → sparse vector (index → weight)
    private final Map<Long, Map<Integer, Double>> vectorCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("PromptEmbeddingService initialized — using keyword-frequency vectors, minKeywordLen={}", minKeywordLength);
    }

    public double[] embed(String text) {
        Map<String, Double> tf = tokenize(text);
        double[] vec = new double[vocabulary.size()];
        for (Map.Entry<String, Double> e : tf.entrySet()) {
            Integer idx = vocabulary.get(e.getKey());
            if (idx != null) {
                vec[idx] = e.getValue();
            }
        }
        return vec;
    }

    public Map<Integer, Double> embedSparse(String text) {
        Map<String, Double> tf = tokenize(text);
        Map<Integer, Double> sparse = new HashMap<>();
        for (Map.Entry<String, Double> e : tf.entrySet()) {
            Integer idx = vocabulary.computeIfAbsent(e.getKey(), k -> vocabulary.size());
            sparse.put(idx, e.getValue());
        }
        return sparse;
    }

    public void index(Long promptId, String text) {
        Map<Integer, Double> vec = embedSparse(text);
        vectorCache.put(promptId, vec);
    }

    public void remove(Long promptId) {
        vectorCache.remove(promptId);
    }

    public void clear() {
        vectorCache.clear();
        vocabulary.clear();
    }

    /**
     * 余弦相似度搜索，返回 topK 个结果（不含 selfId）
     */
    public List<ScoredPrompt> search(String queryText, Long selfId, int topK) {
        Map<Integer, Double> queryVec = embedSparse(queryText);
        if (queryVec.isEmpty()) return Collections.emptyList();

        PriorityQueue<ScoredPrompt> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a.score));

        for (Map.Entry<Long, Map<Integer, Double>> entry : vectorCache.entrySet()) {
            if (entry.getKey().equals(selfId)) continue;
            double similarity = cosineSimilarity(queryVec, entry.getValue());
            if (similarity > 0.05) {
                pq.offer(new ScoredPrompt(entry.getKey(), similarity));
                if (pq.size() > topK) pq.poll();
            }
        }

        List<ScoredPrompt> results = new ArrayList<>(pq);
        results.sort((a, b) -> Double.compare(b.score, a.score));
        return results;
    }

    private Map<String, Double> tokenize(String text) {
        if (text == null || text.isBlank()) return Collections.emptyMap();
        String lower = text.toLowerCase();
        String[] tokens = TOKEN_PATTERN.split(lower);
        Map<String, Double> tf = new LinkedHashMap<>();
        for (String token : tokens) {
            String t = token.trim();
            if (t.length() < minKeywordLength || STOP_WORDS.contains(t)) continue;
            tf.merge(t, 1.0, Double::sum);
        }
        // 归一化
        double norm = Math.sqrt(tf.values().stream().mapToDouble(v -> v * v).sum());
        if (norm > 0) tf.replaceAll((k, v) -> v / norm);
        return tf;
    }

    private double cosineSimilarity(Map<Integer, Double> a, Map<Integer, Double> b) {
        double dot = 0;
        for (Map.Entry<Integer, Double> e : a.entrySet()) {
            Double vb = b.get(e.getKey());
            if (vb != null) dot += e.getValue() * vb;
        }
        return dot; // vectors are already normalized in tokenize()
    }

    public record ScoredPrompt(long promptId, double score) {}
}
