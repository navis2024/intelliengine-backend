package com.aigc.intelliengine.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "模板创建请求")
public class MarketTemplateCreateRequest {
    @NotNull(message = "资产ID不能为空")
    @Schema(description = "资产ID")
    private Long assetId;
    
    @NotBlank(message = "模板标题不能为空")
    @Schema(description = "模板标题")
    private String title;
    
    @Schema(description = "模板描述")
    private String description;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    @Schema(description = "售价")
    private BigDecimal price;
    
    @Schema(description = "原价")
    private BigDecimal originalPrice;
}
