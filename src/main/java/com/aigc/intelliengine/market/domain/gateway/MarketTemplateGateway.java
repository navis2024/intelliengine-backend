package com.aigc.intelliengine.market.domain.gateway;

import com.aigc.intelliengine.market.domain.entity.MarketTemplate;
import java.util.List;
import java.util.Optional;

public interface MarketTemplateGateway {
    MarketTemplate save(MarketTemplate template);
    Optional<MarketTemplate> findById(Long id);
    List<MarketTemplate> findPublished();
    List<MarketTemplate> findByCategory(Long categoryId);
    List<MarketTemplate> findByCreator(Long userId);
    MarketTemplate update(MarketTemplate template);
    boolean remove(Long id);
}
