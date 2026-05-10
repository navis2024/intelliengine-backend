package com.aigc.intelliengine.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "订单信息")
public class MarketOrderVO {
    @Schema(description = "订单ID") private String id;
    @Schema(description = "订单号") private String orderNo;
    @Schema(description = "买家ID") private String buyerId;
    @Schema(description = "总金额") private BigDecimal totalAmount;
    @Schema(description = "应付金额") private BigDecimal payAmount;
    @Schema(description = "状态") private String status;
    @Schema(description = "创建时间") private LocalDateTime createdAt;
    @Schema(description = "订单项") private List<OrderItemVO> items;

    @Data
    public static class OrderItemVO {
        private String templateId;
        private String templateTitle;
        private BigDecimal templatePrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
