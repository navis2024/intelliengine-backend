package com.aigc.intelliengine.market.infrastructure.repository;

import com.aigc.intelliengine.market.domain.entity.MarketTemplate;
import com.aigc.intelliengine.market.domain.gateway.MarketTemplateGateway;
import com.aigc.intelliengine.market.infrastructure.dataobject.MarketTemplateDO;
import com.aigc.intelliengine.market.infrastructure.mapper.MarketTemplateMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MarketTemplateRepositoryImpl implements MarketTemplateGateway {
    private final MarketTemplateMapper templateMapper;
    
    public MarketTemplateRepositoryImpl(MarketTemplateMapper templateMapper) {
        this.templateMapper = Objects.requireNonNull(templateMapper);
    }
    
    @Override
    public MarketTemplate save(MarketTemplate template) {
        MarketTemplateDO templateDO = toDataObject(template);
        templateMapper.insert(templateDO);
        return toEntity(templateDO);
    }
    
    @Override
    public Optional<MarketTemplate> findById(Long id) {
        MarketTemplateDO templateDO = templateMapper.selectById(id);
        return Optional.ofNullable(toEntity(templateDO));
    }
    
    @Override
    public List<MarketTemplate> findPublished() {
        return templateMapper.selectPublished().stream()
            .map(this::toEntity).collect(Collectors.toList());
    }
    
    @Override
    public List<MarketTemplate> findByCategory(Long categoryId) {
        return templateMapper.selectByCategory(categoryId).stream()
            .map(this::toEntity).collect(Collectors.toList());
    }
    
    @Override
    public List<MarketTemplate> findByCreator(Long userId) {
        return templateMapper.selectByCreator(userId).stream()
            .map(this::toEntity).collect(Collectors.toList());
    }
    
    @Override
    public MarketTemplate update(MarketTemplate template) {
        MarketTemplateDO templateDO = toDataObject(template);
        templateMapper.updateById(templateDO);
        return toEntity(templateDO);
    }
    
    @Override
    public boolean remove(Long id) {
        return templateMapper.deleteById(id) > 0;
    }
    
    private MarketTemplateDO toDataObject(MarketTemplate template) {
        if (template == null) return null;
        MarketTemplateDO templateDO = new MarketTemplateDO();
        if (template.getId() != null) templateDO.setId(Long.valueOf(template.getId()));
        if (template.getAssetId() != null) templateDO.setAssetId(Long.valueOf(template.getAssetId()));
        templateDO.setTitle(template.getTitle());
        templateDO.setDescription(template.getDescription());
        if (template.getCategoryId() != null) templateDO.setCategoryId(Long.valueOf(template.getCategoryId()));
        templateDO.setPrice(template.getPrice());
        templateDO.setOriginalPrice(template.getOriginalPrice());
        templateDO.setCurrency(template.getCurrency());
        templateDO.setSalesCount(template.getSalesCount());
        templateDO.setViewCount(template.getViewCount());
        templateDO.setRating(template.getRating());
        templateDO.setStatus(template.getStatus());
        if (template.getCreatedBy() != null) templateDO.setCreatedBy(Long.valueOf(template.getCreatedBy()));
        templateDO.setCreatedAt(template.getCreateTime());
        templateDO.setUpdatedAt(template.getUpdateTime());
        templateDO.setIsDeleted(template.getDeleted());
        return templateDO;
    }
    
    private MarketTemplate toEntity(MarketTemplateDO templateDO) {
        if (templateDO == null) return null;
        MarketTemplate template = new MarketTemplate();
        template.setId(String.valueOf(templateDO.getId()));
        template.setAssetId(String.valueOf(templateDO.getAssetId()));
        template.setTitle(templateDO.getTitle());
        template.setDescription(templateDO.getDescription());
        template.setCategoryId(templateDO.getCategoryId() != null ? String.valueOf(templateDO.getCategoryId()) : null);
        template.setPrice(templateDO.getPrice());
        template.setOriginalPrice(templateDO.getOriginalPrice());
        template.setCurrency(templateDO.getCurrency());
        template.setSalesCount(templateDO.getSalesCount());
        template.setViewCount(templateDO.getViewCount());
        template.setRating(templateDO.getRating());
        template.setStatus(templateDO.getStatus());
        template.setCreatedBy(templateDO.getCreatedBy() != null ? String.valueOf(templateDO.getCreatedBy()) : null);
        template.setCreateTime(templateDO.getCreatedAt());
        template.setUpdateTime(templateDO.getUpdatedAt());
        template.setDeleted(templateDO.getIsDeleted());
        return template;
    }
}
