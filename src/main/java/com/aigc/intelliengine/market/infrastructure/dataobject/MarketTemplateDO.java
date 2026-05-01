package com.aigc.intelliengine.market.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("market_template")
public class MarketTemplateDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private String title;
    private String description;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String currency;
    private Integer salesCount;
    private Integer viewCount;
    private BigDecimal rating;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
