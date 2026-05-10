package com.aigc.intelliengine.asset.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset_info")
public class AssetInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetCode;
    private String name;
    private String type;
    private String ownerType;
    private Long ownerId;
    private Long sourceAssetId;
    private Integer sourceVersion;
    private Integer version;
    private Integer isLatest;
    private String commitMessage;
    private Long committedBy;
    private LocalDateTime committedAt;
    private String status;
    private String fileUrl;
    private Long fileSize;
    private String fileFormat;
    private Integer duration;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
