package com.aigc.intelliengine.market.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("market_order")
public class MarketOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long buyerId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String currency;
    private String status;
    private LocalDateTime payTime;
    private String payChannel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
