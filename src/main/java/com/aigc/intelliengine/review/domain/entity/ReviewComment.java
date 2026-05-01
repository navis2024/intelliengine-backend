package com.aigc.intelliengine.review.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReviewComment {
    private String id;
    private String assetId;
    private String projectId;
    private String content;
    private String commentType;
    private BigDecimal timestamp;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
