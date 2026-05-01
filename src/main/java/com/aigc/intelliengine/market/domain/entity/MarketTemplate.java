package com.aigc.intelliengine.market.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场模板领域实体
 */
@Data
public class MarketTemplate {
    private String id;
    private String assetId;
    private String title;
    private String description;
    private String categoryId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String currency;
    private Integer salesCount;
    private Integer viewCount;
    private BigDecimal rating;
    private String status;
    private String createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
