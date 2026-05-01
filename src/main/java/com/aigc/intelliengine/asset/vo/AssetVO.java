package com.aigc.intelliengine.asset.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "资产信息")
public class AssetVO {
    @Schema(description = "资产ID")
    private String id;
    @Schema(description = "资产编码")
    private String assetCode;
    @Schema(description = "资产名称")
    private String name;
    @Schema(description = "资产类型")
    private String type;
    @Schema(description = "所有者类型")
    private String ownerType;
    @Schema(description = "所有者ID")
    private String ownerId;
    @Schema(description = "版本号")
    private Integer version;
    @Schema(description = "状态: DRAFT/REVIEW/APPROVED")
    private String status;
    @Schema(description = "文件URL")
    private String fileUrl;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
