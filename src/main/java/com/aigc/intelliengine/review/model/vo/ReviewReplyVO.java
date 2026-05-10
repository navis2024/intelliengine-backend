package com.aigc.intelliengine.review.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "回复信息")
public class ReviewReplyVO {
    @Schema(description = "回复ID") private String id;
    @Schema(description = "评论ID") private String commentId;
    @Schema(description = "内容") private String content;
    @Schema(description = "创建者ID") private String createdBy;
    @Schema(description = "创建者用户名") private String username;
    @Schema(description = "创建时间") private LocalDateTime createdAt;
}
