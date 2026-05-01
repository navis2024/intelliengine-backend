package com.aigc.intelliengine.market.domain.gateway;

import com.aigc.intelliengine.market.domain.entity.MarketOrder;
import java.util.List;
import java.util.Optional;

public interface MarketOrderGateway {
    MarketOrder save(MarketOrder order);
    Optional<MarketOrder> findById(Long id);
    Optional<MarketOrder> findByOrderNo(String orderNo);
    List<MarketOrder> findByBuyer(Long buyerId);
    MarketOrder update(MarketOrder order);
}
