package com.aigc.intelliengine.asset.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产领域实体(Asset Entity)
 */
@Data
public class Asset {
    private String id;
    private String assetCode;
    private String name;
    private String type;
    private String ownerType;
    private String ownerId;
    private String sourceAssetId;
    private Integer sourceVersion;
    private Integer version;
    private Integer latest;
    private String commitMessage;
    private String committedBy;
    private LocalDateTime committedAt;
    private String status;
    private String fileUrl;
    private Long fileSize;
    private String fileFormat;
    private Integer duration;
    private String createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
