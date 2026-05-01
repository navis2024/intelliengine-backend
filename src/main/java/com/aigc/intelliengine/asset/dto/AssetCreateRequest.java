package com.aigc.intelliengine.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "资产创建请求")
public class AssetCreateRequest {
    @NotBlank(message = "资产名称不能为空")
    @Schema(description = "资产名称", required = true)
    private String name;
    
    @NotBlank(message = "资产类型不能为空")
    @Schema(description = "资产类型: VIDEO/IMAGE/AUDIO/TEMPLATE", example = "VIDEO")
    private String type;
    
    @NotBlank(message = "所有者类型不能为空")
    @Schema(description = "所有者类型: USER/PROJECT", example = "USER")
    private String ownerType;
    
    @NotNull(message = "所有者ID不能为空")
    @Schema(description = "所有者ID", example = "1")
    private Long ownerId;
}
