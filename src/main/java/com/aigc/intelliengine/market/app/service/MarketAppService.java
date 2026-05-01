package com.aigc.intelliengine.market.app.service;

import com.aigc.intelliengine.market.domain.entity.MarketTemplate;
import com.aigc.intelliengine.market.dto.MarketTemplateCreateRequest;
import com.aigc.intelliengine.market.infrastructure.dataobject.MarketTemplateDO;
import com.aigc.intelliengine.market.infrastructure.mapper.MarketTemplateMapper;
import com.aigc.intelliengine.market.vo.MarketTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketAppService {
    private final MarketTemplateMapper templateMapper;
    
    @Transactional
    public MarketTemplateVO createTemplate(MarketTemplateCreateRequest request, Long userId) {
        MarketTemplateDO template = new MarketTemplateDO();
        template.setAssetId(request.getAssetId());
        template.setTitle(request.getTitle());
        template.setDescription(request.getDescription());
        template.setCategoryId(request.getCategoryId());
        template.setPrice(request.getPrice());
        template.setOriginalPrice(request.getOriginalPrice());
        template.setCurrency("CNY");
        template.setSalesCount(0);
        template.setViewCount(0);
        template.setRating(new BigDecimal("5.0"));
        template.setStatus("DRAFT");
        template.setCreatedBy(userId);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setIsDeleted(0);
        
        templateMapper.insert(template);
        return toVO(template);
    }
    
    public List<MarketTemplateVO> getPublishedTemplates() {
        return templateMapper.selectPublished().stream()
            .map(this::toVO).collect(Collectors.toList());
    }
    
    public MarketTemplateVO getTemplateById(Long id) {
        MarketTemplateDO template = templateMapper.selectById(id);
        return template != null ? toVO(template) : null;
    }
    
    private MarketTemplateVO toVO(MarketTemplateDO template) {
        MarketTemplateVO vo = new MarketTemplateVO();
        vo.setId(String.valueOf(template.getId()));
        vo.setAssetId(String.valueOf(template.getAssetId()));
        vo.setTitle(template.getTitle());
        vo.setDescription(template.getDescription());
        vo.setPrice(template.getPrice());
        vo.setOriginalPrice(template.getOriginalPrice());
        vo.setSalesCount(template.getSalesCount());
        vo.setViewCount(template.getViewCount());
        vo.setRating(template.getRating());
        vo.setStatus(template.getStatus());
        vo.setCreateTime(template.getCreatedAt());
        return vo;
    }
}
