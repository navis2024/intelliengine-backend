package com.aigc.intelliengine.review.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建回复请求")
public class ReplyCreateRequest {
    @NotNull @Schema(description = "评论ID", required = true)
    private Long commentId;
    @NotBlank @Schema(description = "回复内容", required = true)
    private String content;
}
