package com.aigc.intelliengine.review.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "评论/批注信息")
public class ReviewCommentVO {
    @Schema(description = "评论ID") private String id;
    @Schema(description = "资产ID") private String assetId;
    @Schema(description = "内容") private String content;
    @Schema(description = "类型") private String commentType;
    @Schema(description = "时间戳") private BigDecimal timestamp;
    @Schema(description = "X坐标") private BigDecimal positionX;
    @Schema(description = "Y坐标") private BigDecimal positionY;
    @Schema(description = "状态") private String status;
    @Schema(description = "创建者ID") private String createdBy;
    @Schema(description = "创建者用户名") private String username;
    @Schema(description = "创建时间") private LocalDateTime createdAt;
    @Schema(description = "回复列表") private List<ReviewReplyVO> replies;
}
