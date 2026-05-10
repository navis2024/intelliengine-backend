package com.aigc.intelliengine.asset.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "资产更新请求")
public class AssetUpdateRequest {
    @Schema(description = "资产名称")
    private String name;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "描述/提交信息")
    private String commitMessage;
}
