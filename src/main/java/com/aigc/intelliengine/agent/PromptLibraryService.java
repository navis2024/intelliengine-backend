package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.PromptLibrary;
import com.aigc.intelliengine.agent.model.vo.SemanticPromptVO;
import com.aigc.intelliengine.agent.rag.PromptEmbeddingService;
import com.aigc.intelliengine.agent.rag.RagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptLibraryService {

    private final PromptLibraryMapper promptLibraryMapper;
    private final RagService ragService;

    @PostConstruct
    public void initIndex() {
        List<PromptLibrary> all = promptLibraryMapper.selectList(
                new LambdaQueryWrapper<PromptLibrary>().eq(PromptLibrary::getIsDeleted, 0));
        for (PromptLibrary p : all) {
            ragService.index(p.getId(), p.getPromptText());
        }
        log.info("RAG index built: {} prompts indexed", all.size());
    }

    @Transactional
    public PromptLibrary create(PromptLibrary prompt) {
        prompt.setCreatedAt(LocalDateTime.now());
        prompt.setUpdatedAt(LocalDateTime.now());
        prompt.setUseCount(0);
        promptLibraryMapper.insert(prompt);
        ragService.index(prompt.getId(), prompt.getPromptText());
        return prompt;
    }

    public PromptLibrary findById(Long id) { return promptLibraryMapper.selectById(id); }

    /** 传统关键词搜索 */
    public List<PromptLibrary> search(String keyword, String promptType, String styleTag, int page, int size) {
        LambdaQueryWrapper<PromptLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptLibrary::getIsDeleted, 0);
        if (keyword != null && !keyword.isBlank()) wrapper.like(PromptLibrary::getPromptText, keyword);
        if (promptType != null && !promptType.isBlank()) wrapper.eq(PromptLibrary::getPromptType, promptType);
        if (styleTag != null && !styleTag.isBlank()) wrapper.like(PromptLibrary::getStyleTags, styleTag);
        wrapper.orderByDesc(PromptLibrary::getUseCount);
        return promptLibraryMapper.selectPage(Page.of(page, size), wrapper).getRecords();
    }

    /** RAG语义检索 — 基于向量相似度，返回带分数的结果 */
    public List<SemanticPromptVO> semanticSearch(String queryText, int topK) {
        List<PromptEmbeddingService.ScoredPrompt> scored = ragService.search(queryText, null, topK);
        if (scored.isEmpty()) return List.of();

        List<Long> ids = scored.stream().map(PromptEmbeddingService.ScoredPrompt::promptId).toList();
        Map<Long, Double> scoreMap = scored.stream()
                .collect(Collectors.toMap(PromptEmbeddingService.ScoredPrompt::promptId, PromptEmbeddingService.ScoredPrompt::score));

        List<PromptLibrary> prompts = promptLibraryMapper.selectBatchIds(ids);
        Map<Long, PromptLibrary> promptMap = prompts.stream()
                .collect(Collectors.toMap(PromptLibrary::getId, p -> p));

        List<SemanticPromptVO> result = new ArrayList<>();
        for (Long id : ids) {
            PromptLibrary p = promptMap.get(id);
            if (p == null || p.getIsDeleted() == 1) continue;
            result.add(toSemanticVO(p, scoreMap.getOrDefault(id, 0.0)));
        }
        return result;
    }

    /** 重建全量索引 */
    public int reindexAll() {
        ragService.clear();
        List<PromptLibrary> all = promptLibraryMapper.selectList(
                new LambdaQueryWrapper<PromptLibrary>().eq(PromptLibrary::getIsDeleted, 0));
        for (PromptLibrary p : all) {
            ragService.index(p.getId(), p.getPromptText());
        }
        log.info("RAG index rebuilt: {} prompts", all.size());
        return all.size();
    }

    @Transactional
    public void recordUse(Long id) {
        PromptLibrary p = promptLibraryMapper.selectById(id);
        if (p != null) {
            p.setUseCount(p.getUseCount() == null ? 1 : p.getUseCount() + 1);
            p.setUpdatedAt(LocalDateTime.now());
            promptLibraryMapper.updateById(p);
        }
    }

    @Transactional
    public void deleteById(Long id) {
        PromptLibrary p = promptLibraryMapper.selectById(id);
        if (p != null) {
            p.setIsDeleted(1); p.setUpdatedAt(LocalDateTime.now());
            promptLibraryMapper.updateById(p);
            ragService.remove(id);
        }
    }

    private SemanticPromptVO toSemanticVO(PromptLibrary p, double score) {
        SemanticPromptVO vo = new SemanticPromptVO();
        vo.setId(p.getId());
        vo.setPromptText(p.getPromptText());
        vo.setPromptType(p.getPromptType());
        vo.setStyleTags(p.getStyleTags());
        vo.setSourceVideoId(p.getSourceVideoId());
        vo.setSourceFrameId(p.getSourceFrameId());
        vo.setUseCount(p.getUseCount());
        vo.setRating(p.getRating());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setSimilarityScore(Math.round(score * 10000.0) / 10000.0);
        return vo;
    }
}
