package com.aigc.intelliengine.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "回复创建请求")
public class ReplyCreateRequest {
    @NotNull(message = "批注ID不能为空")
    @Schema(description = "批注ID")
    private Long commentId;
    
    @NotBlank(message = "回复内容不能为空")
    @Schema(description = "回复内容")
    private String content;
}
