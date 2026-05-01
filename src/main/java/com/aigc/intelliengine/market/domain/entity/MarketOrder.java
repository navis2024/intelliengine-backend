package com.aigc.intelliengine.market.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场订单领域实体
 */
@Data
public class MarketOrder {
    private String id;
    private String orderNo;
    private String buyerId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String currency;
    private String status;
    private LocalDateTime payTime;
    private String payChannel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
