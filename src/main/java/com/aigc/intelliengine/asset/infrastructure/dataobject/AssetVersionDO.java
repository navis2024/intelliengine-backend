package com.aigc.intelliengine.asset.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产版本数据对象
 * 对应表: asset_version
 */
@Data
@TableName("asset_version")
public class AssetVersionDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Integer versionNumber;
    private String snapshotData;
    private String changeLog;
    private Long createdBy;
    private LocalDateTime createdAt;
}
