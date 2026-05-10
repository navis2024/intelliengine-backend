package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.PromptLibrary;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptLibraryService {

    private final PromptLibraryMapper promptLibraryMapper;

    @Transactional
    public PromptLibrary create(PromptLibrary prompt) {
        prompt.setCreatedAt(LocalDateTime.now());
        prompt.setUpdatedAt(LocalDateTime.now());
        prompt.setUseCount(0);
        promptLibraryMapper.insert(prompt);
        return prompt;
    }

    public PromptLibrary findById(Long id) { return promptLibraryMapper.selectById(id); }

    public List<PromptLibrary> search(String keyword, String promptType, String styleTag, int page, int size) {
        LambdaQueryWrapper<PromptLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptLibrary::getIsDeleted, 0);
        if (keyword != null && !keyword.isBlank()) wrapper.like(PromptLibrary::getPromptText, keyword);
        if (promptType != null && !promptType.isBlank()) wrapper.eq(PromptLibrary::getPromptType, promptType);
        if (styleTag != null && !styleTag.isBlank()) wrapper.like(PromptLibrary::getStyleTags, styleTag);
        wrapper.orderByDesc(PromptLibrary::getUseCount);
        return promptLibraryMapper.selectPage(Page.of(page, size), wrapper).getRecords();
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
        if (p != null) { p.setIsDeleted(1); p.setUpdatedAt(LocalDateTime.now()); promptLibraryMapper.updateById(p); }
    }
}
