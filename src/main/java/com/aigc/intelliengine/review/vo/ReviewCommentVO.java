package com.aigc.intelliengine.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "批注信息")
public class ReviewCommentVO {
    @Schema(description = "批注ID")
    private String id;
    @Schema(description = "资产ID")
    private String assetId;
    @Schema(description = "项目ID")
    private String projectId;
    @Schema(description = "批注内容")
    private String content;
    @Schema(description = "批注类型")
    private String commentType;
    @Schema(description = "视频时间戳")
    private BigDecimal timestamp;
    @Schema(description = "画布X坐标")
    private BigDecimal positionX;
    @Schema(description = "画布Y坐标")
    private BigDecimal positionY;
    @Schema(description = "状态: OPEN/RESOLVED/CLOSED")
    private String status;
    @Schema(description = "创建者ID")
    private String createdBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "回复列表")
    private List<ReviewReplyVO> replies;
}
