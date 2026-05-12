package com.aigc.intelliengine.review.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "创建评论/批注请求")
public class CommentCreateRequest {
    @NotNull @Schema(description = "资产ID", required = true)
    private Long assetId;
    @NotNull @Schema(description = "项目ID", required = true)
    private Long projectId;
    @NotBlank @Schema(description = "评论内容", required = true)
    private String content;
    @Schema(description = "视频时间戳(秒)")
    private BigDecimal timestamp;
    @Schema(description = "批注X坐标")
    private BigDecimal positionX;
    @Schema(description = "批注Y坐标")
    private BigDecimal positionY;
}
