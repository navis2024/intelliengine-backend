package com.aigc.intelliengine.review.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewReply {
    private String id;
    private String commentId;
    private String content;
    private String createdBy;
    private LocalDateTime createdAt;
    private Integer deleted;
}
