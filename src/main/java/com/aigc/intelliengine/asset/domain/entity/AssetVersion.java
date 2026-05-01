package com.aigc.intelliengine.asset.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssetVersion {
    private String id;
    private String assetId;
    private Integer versionNumber;
    private String snapshotData;
    private String changeLog;
    private String createdBy;
    private LocalDateTime createdAt;
}
