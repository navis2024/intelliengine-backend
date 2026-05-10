package com.aigc.intelliengine.market.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "创建订单请求")
public class OrderCreateRequest {
    @NotNull @Schema(description = "订单项", required = true)
    private List<OrderItem> items;

    @Data
    @Schema(description = "订单项")
    public static class OrderItem {
        @NotNull @Schema(description = "模板ID", required = true)
        private Long templateId;
        @Schema(description = "数量", example = "1")
        private Integer quantity = 1;
    }
}
