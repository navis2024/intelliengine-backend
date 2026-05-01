package com.aigc.intelliengine.asset.adapter.web;

import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.asset.app.service.AssetAppService;
import com.aigc.intelliengine.asset.dto.AssetCreateRequest;
import com.aigc.intelliengine.asset.vo.AssetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "资产管理", description = "资产上传、版本管理、查询")
public class AssetController {
    private final AssetAppService assetAppService;
    
    @PostMapping
    @Operation(summary = "创建资产")
    public ApiResponse<AssetVO> createAsset(@Valid @RequestBody AssetCreateRequest request) {
        Long userId = 1L; // TODO: 从token获取
        return ApiResponse.success(assetAppService.createAsset(request, userId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取资产详情")
    public ApiResponse<AssetVO> getAsset(@PathVariable Long id) {
        return ApiResponse.success(assetAppService.getAssetById(id));
    }
    
    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "查询指定所有者的资产")
    public ApiResponse<List<AssetVO>> getAssetsByOwner(
            @PathVariable Long ownerId,
            @RequestParam String ownerType) {
        return ApiResponse.success(assetAppService.getAssetsByOwner(ownerId, ownerType));
    }
}
