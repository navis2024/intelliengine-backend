package com.aigc.intelliengine.market.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "市场模板创建请求")
public class MarketTemplateCreateRequest {
    @NotNull @Schema(description = "资产ID", required = true)
    private Long assetId;
    @NotBlank @Schema(description = "标题", required = true)
    private String title;
    @Schema(description = "描述")
    private String description;
    @NotNull @Schema(description = "价格", example = "9.99")
    private BigDecimal price;
    @Schema(description = "原价")
    private BigDecimal originalPrice;
}
