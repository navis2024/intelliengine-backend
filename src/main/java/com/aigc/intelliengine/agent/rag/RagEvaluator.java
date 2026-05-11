package com.aigc.intelliengine.agent.rag;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * RAG检索质量评估器.
 *
 * 指标:
 *   - Recall@K: 前K个结果中命中期盼文档的比例 (越高越好)
 *   - MRR (Mean Reciprocal Rank): 第一个相关结果的排名倒数均值 (越高越好)
 *
 * 用于验证检索策略变更是否真正有效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagEvaluator {

    private final RagService ragService;

    /** 评估结果 */
    @Data
    public static class EvalResult {
        private double recallAt1;
        private double recallAt3;
        private double recallAt5;
        private double recallAt10;
        private double mrr;
        private int totalQueries;
        private int successfulQueries;
        private List<QueryResult> details;
    }

    @Data
    public static class QueryResult {
        private String query;
        private int expectedCount;
        private int foundAtK3;
        private int foundAtK5;
        private double reciprocalRank;
        private List<ScoredItem> topResults;
    }

    @Data
    public static class ScoredItem {
        private long promptId;
        private double score;
    }

    /**
     * 运行评估.
     *
     * @param testCases  测试用例: query → 期望匹配的promptId集合
     * @param topK       检索返回条数
     */
    public EvalResult evaluate(Map<String, Set<Long>> testCases, int topK) {
        List<QueryResult> details = new ArrayList<>();
        int totalQueries = testCases.size();
        int successfulQueries = 0;
        double sumRR = 0;
        double sumRecall1 = 0, sumRecall3 = 0, sumRecall5 = 0, sumRecall10 = 0;

        for (Map.Entry<String, Set<Long>> entry : testCases.entrySet()) {
            String query = entry.getKey();
            Set<Long> expected = entry.getValue();

            List<PromptEmbeddingService.ScoredPrompt> searchResults =
                    ragService.search(query, null, topK);

            QueryResult qr = new QueryResult();
            qr.setQuery(query);
            qr.setExpectedCount(expected.size());
            qr.setTopResults(searchResults.stream()
                    .map(s -> { ScoredItem si = new ScoredItem(); si.setPromptId(s.promptId()); si.setScore(s.score()); return si; })
                    .toList());

            // Recall@K
            int foundAt3 = countHits(searchResults, expected, 3);
            int foundAt5 = countHits(searchResults, expected, 5);
            int foundAt1 = countHits(searchResults, expected, 1);
            int foundAt10 = countHits(searchResults, expected, 10);

            qr.setFoundAtK3(foundAt3);
            qr.setFoundAtK5(foundAt5);

            int expectedSize = expected.isEmpty() ? 1 : expected.size();
            sumRecall1 += (double) foundAt1 / expectedSize;
            sumRecall3 += (double) foundAt3 / expectedSize;
            sumRecall5 += (double) foundAt5 / expectedSize;
            sumRecall10 += (double) foundAt10 / expectedSize;

            if (foundAt5 > 0) successfulQueries++;

            // MRR: 1 / rank of first hit
            double rr = 0;
            for (int i = 0; i < searchResults.size(); i++) {
                if (expected.contains(searchResults.get(i).promptId())) {
                    rr = 1.0 / (i + 1);
                    break;
                }
            }
            qr.setReciprocalRank(rr);
            sumRR += rr;

            details.add(qr);
        }

        EvalResult result = new EvalResult();
        result.setRecallAt1(round(sumRecall1 / totalQueries));
        result.setRecallAt3(round(sumRecall3 / totalQueries));
        result.setRecallAt5(round(sumRecall5 / totalQueries));
        result.setRecallAt10(round(sumRecall10 / totalQueries));
        result.setMrr(round(sumRR / totalQueries));
        result.setTotalQueries(totalQueries);
        result.setSuccessfulQueries(successfulQueries);
        result.setDetails(details);

        log.info("RAG Evaluation: Recall@1={}, Recall@3={}, Recall@5={}, Recall@10={}, MRR={}, success={}/{}",
                result.getRecallAt1(), result.getRecallAt3(), result.getRecallAt5(),
                result.getRecallAt10(), result.getMrr(), successfulQueries, totalQueries);

        return result;
    }

    private int countHits(List<PromptEmbeddingService.ScoredPrompt> results, Set<Long> expected, int k) {
        int count = 0;
        for (int i = 0; i < Math.min(results.size(), k); i++) {
            if (expected.contains(results.get(i).promptId())) count++;
        }
        return count;
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
