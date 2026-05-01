package com.aigc.intelliengine.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "批注创建请求")
public class CommentCreateRequest {
    @NotNull(message = "资产ID不能为空")
    @Schema(description = "资产ID")
    private Long assetId;
    
    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID")
    private Long projectId;
    
    @NotBlank(message = "批注内容不能为空")
    @Schema(description = "批注内容")
    private String content;
    
    @Schema(description = "批注类型: COMMENT/ISSUE/NOTE", example = "COMMENT")
    private String commentType;
    
    @Schema(description = "视频时间戳(秒)", example = "120.50")
    private BigDecimal timestamp;
    
    @Schema(description = "画布X坐标(0-100)", example = "50.0")
    private BigDecimal positionX;
    
    @Schema(description = "画布Y坐标(0-100)", example = "30.0")
    private BigDecimal positionY;
}
