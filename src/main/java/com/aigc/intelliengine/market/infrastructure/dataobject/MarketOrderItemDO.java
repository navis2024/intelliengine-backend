package com.aigc.intelliengine.market.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单明细数据对象
 * 对应表: market_order_item
 */
@Data
@TableName("market_order_item")
public class MarketOrderItemDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long templateId;
    private String templateName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
