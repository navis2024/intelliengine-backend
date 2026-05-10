package com.aigc.intelliengine.asset.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "资产创建请求")
public class AssetCreateRequest {
    @NotBlank @Schema(description = "资产名称", required = true)
    private String name;
    @NotBlank @Schema(description = "类型: VIDEO/IMAGE/AUDIO/TEMPLATE", example = "VIDEO")
    private String type;
    @NotBlank @Schema(description = "所有者类型: USER/PROJECT", example = "USER")
    private String ownerType;
    @NotNull @Schema(description = "所有者ID", example = "1")
    private Long ownerId;
}
