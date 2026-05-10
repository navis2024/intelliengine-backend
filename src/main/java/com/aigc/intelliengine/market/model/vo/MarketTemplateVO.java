package com.aigc.intelliengine.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "市场模板信息")
public class MarketTemplateVO {
    @Schema(description = "模板ID") private String id;
    @Schema(description = "资产ID") private String assetId;
    @Schema(description = "标题") private String title;
    @Schema(description = "描述") private String description;
    @Schema(description = "价格") private BigDecimal price;
    @Schema(description = "原价") private BigDecimal originalPrice;
    @Schema(description = "销量") private Integer salesCount;
    @Schema(description = "浏览量") private Integer viewCount;
    @Schema(description = "评分") private BigDecimal rating;
    @Schema(description = "状态") private String status;
    @Schema(description = "创建者ID") private String createdBy;
    @Schema(description = "创建时间") private LocalDateTime createdAt;
}
