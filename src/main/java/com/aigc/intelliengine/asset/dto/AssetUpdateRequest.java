package com.aigc.intelliengine.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "资产更新请求")
public class AssetUpdateRequest {
    @Schema(description = "资产名称")
    private String name;
    @Schema(description = "资产状态: DRAFT/REVIEW/APPROVED")
    private String status;
    @Schema(description = "文件URL")
    private String fileUrl;
}
