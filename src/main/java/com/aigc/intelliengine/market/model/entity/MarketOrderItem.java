package com.aigc.intelliengine.market.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("market_order_item")
public class MarketOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long templateId;
    private String templateTitle;
    private BigDecimal templatePrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
