package com.aigc.intelliengine.asset;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.security.UserContextHolder;
import com.aigc.intelliengine.asset.model.dto.AssetCreateRequest;
import com.aigc.intelliengine.asset.model.dto.AssetUpdateRequest;
import com.aigc.intelliengine.asset.model.entity.AssetVersion;
import com.aigc.intelliengine.asset.model.vo.AssetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Asset", description = "资产管理")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @Operation(summary = "创建资产")
    public ApiResponse<AssetVO> create(@Valid @RequestBody AssetCreateRequest request) {
        return ApiResponse.success(assetService.createAsset(request, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取资产详情")
    public ApiResponse<AssetVO> getById(@PathVariable Long id) {
        return ApiResponse.success(assetService.getAsset(id, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping
    @Operation(summary = "资产列表")
    public ApiResponse<PageResult<AssetVO>> list(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        return ApiResponse.success(assetService.listAssets(ownerId, ownerType, type, status, pageNum, pageSize, UserContextHolder.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新资产")
    public ApiResponse<AssetVO> update(@PathVariable Long id, @Valid @RequestBody AssetUpdateRequest request) {
        return ApiResponse.success(assetService.updateAsset(id, request, UserContextHolder.getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资产")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "版本历史")
    public ApiResponse<List<AssetVersion>> getVersions(@PathVariable Long id) {
        return ApiResponse.success(assetService.getVersions(id));
    }

    @PostMapping("/{id}/versions")
    @Operation(summary = "创建新版本")
    public ApiResponse<AssetVersion> createVersion(@PathVariable Long id, @RequestParam String changeLog) {
        return ApiResponse.success(assetService.createVersion(id, changeLog, UserContextHolder.getCurrentUserId()));
    }

    @PostMapping("/{id}/rollback/{versionNumber}")
    @Operation(summary = "回滚到指定版本")
    public ApiResponse<AssetVO> rollback(@PathVariable Long id, @PathVariable Integer versionNumber) {
        return ApiResponse.success(assetService.rollbackToVersion(id, versionNumber, UserContextHolder.getCurrentUserId()));
    }

    @PostMapping("/{id}/link-to-project")
    @Operation(summary = "将资产关联到项目")
    public ApiResponse<AssetVO> linkToProject(@PathVariable Long id, @RequestParam Long projectId) {
        return ApiResponse.success(assetService.linkToProject(id, projectId, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/{id}/versions/diff")
    @Operation(summary = "对比两个版本")
    public ApiResponse<Map<String, Object>> diffVersions(@PathVariable Long id, @RequestParam Integer v1, @RequestParam Integer v2) {
        return ApiResponse.success(assetService.diffVersions(id, v1, v2));
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public ApiResponse<AssetVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "ownerType", defaultValue = "USER") String ownerType,
            @RequestParam(value = "ownerId", required = false) Long ownerId,
            @RequestParam(value = "assetId", required = false) Long assetId) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (assetId != null) {
            return ApiResponse.success(assetService.uploadNewVersion(assetId, file, userId));
        }
        return ApiResponse.success(assetService.uploadFile(file, name, type, ownerType, ownerId, userId));
    }
}
