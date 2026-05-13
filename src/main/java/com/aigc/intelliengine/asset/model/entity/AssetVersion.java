package com.aigc.intelliengine.asset.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset_version")
public class AssetVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Integer versionNumber;
    private String snapshotData;
    private String changeLog;
    private String agentAdvice;
    private Long createdBy;
    private LocalDateTime createdAt;
}
