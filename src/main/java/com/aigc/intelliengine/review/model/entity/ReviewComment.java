package com.aigc.intelliengine.review.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review_comment")
public class ReviewComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Long projectId;
    private String content;
    private String commentType;
    private BigDecimal timestamp;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
