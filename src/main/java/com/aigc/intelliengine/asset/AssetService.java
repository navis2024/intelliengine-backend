package com.aigc.intelliengine.asset;

import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.aigc.intelliengine.asset.model.dto.AssetCreateRequest;
import com.aigc.intelliengine.asset.model.dto.AssetUpdateRequest;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.asset.model.entity.AssetVersion;
import com.aigc.intelliengine.asset.model.vo.AssetVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetMapper assetMapper;
    private final AssetVersionMapper versionMapper;
    private final FileStorageService fileStorageService;
    private final MembershipValidator validator;

    @Transactional
    public AssetVO createAsset(AssetCreateRequest request, Long userId) {
        if ("PROJECT".equals(request.getOwnerType()) && request.getOwnerId() != null) {
            validator.requireMembership(request.getOwnerId(), userId);
        }
        AssetInfo asset = new AssetInfo();
        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setOwnerType(request.getOwnerType());
        asset.setOwnerId(request.getOwnerId());
        asset.setStatus("UPLOADING");
        asset.setVersion(1);
        asset.setIsLatest(1);
        asset.setCreatedBy(userId);
        asset.setAssetCode("AST_" + System.currentTimeMillis());
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        assetMapper.insert(asset);
        return toVO(asset);
    }

    public AssetVO getAsset(Long id, Long userId) {
        AssetInfo asset = validator.requireAssetAccess(id, userId);
        return toVO(asset);
    }

    public PageResult<AssetVO> listAssets(Long ownerId, String ownerType, String type, String status, Integer pageNum, Integer pageSize, Long userId) {
        LambdaQueryWrapper<AssetInfo> wrapper = new LambdaQueryWrapper<>();
        if (ownerId != null) wrapper.eq(AssetInfo::getOwnerId, ownerId);
        if (ownerType != null) wrapper.eq(AssetInfo::getOwnerType, ownerType);
        if (type != null) wrapper.eq(AssetInfo::getType, type);
        if (status != null) wrapper.eq(AssetInfo::getStatus, status);
        wrapper.orderByDesc(AssetInfo::getCreatedAt);
        Page<AssetInfo> page = assetMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords().stream().map(this::toVO).toList(), page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public AssetVO updateAsset(Long id, AssetUpdateRequest request, Long userId) {
        AssetInfo asset = assetMapper.selectById(id);
        if (asset == null) throw new BusinessException("资产不存在");
        if (!asset.getCreatedBy().equals(userId))
            throw new BusinessException(403, "只有资产所有者可以更新");
        if (request.getName() != null) asset.setName(request.getName());
        if (request.getStatus() != null) asset.setStatus(request.getStatus());
        if (request.getCommitMessage() != null) asset.setCommitMessage(request.getCommitMessage());
        asset.setUpdatedAt(LocalDateTime.now());
        assetMapper.updateById(asset);
        return toVO(asset);
    }

    @Transactional
    public AssetVO linkToProject(Long assetId, Long projectId, Long userId) {
        AssetInfo asset = assetMapper.selectById(assetId);
        if (asset == null) throw new BusinessException("资产不存在");
        if (!asset.getCreatedBy().equals(userId))
            throw new BusinessException(403, "只有资产所有者可以关联");
        validator.requireMembership(projectId, userId);
        asset.setOwnerType("PROJECT");
        asset.setOwnerId(projectId);
        asset.setUpdatedAt(LocalDateTime.now());
        assetMapper.updateById(asset);
        return toVO(asset);
    }

    public String getPlayUrl(Long assetId, Long userId) {
        AssetInfo asset = validator.requireAssetAccess(assetId, userId);
        if (asset.getFileUrl() == null) throw new BusinessException("资产文件不存在");
        return fileStorageService.getPresignedUrl(asset.getFileUrl());
    }

    @Transactional
    public void deleteAsset(Long id, Long userId) {
        AssetInfo asset = assetMapper.selectById(id);
        if (asset == null) throw new BusinessException("资产不存在");
        if (!asset.getCreatedBy().equals(userId))
            throw new BusinessException(403, "只有资产所有者可以删除");
        assetMapper.deleteById(id);
    }

    public List<AssetVersion> getVersions(Long assetId) { return versionMapper.selectByAsset(assetId); }

    @Transactional
    public AssetVersion createVersion(Long assetId, String changeLog, Long userId) {
        AssetInfo asset = assetMapper.selectById(assetId);
        if (asset == null) throw new BusinessException("资产不存在");
        List<AssetVersion> existing = versionMapper.selectByAsset(assetId);
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersionNumber() + 1;

        AssetVersion version = new AssetVersion();
        version.setAssetId(assetId);
        version.setVersionNumber(nextVersion);
        version.setChangeLog(changeLog);
        version.setCreatedBy(userId);
        version.setCreatedAt(LocalDateTime.now());
        versionMapper.insert(version);

        asset.setVersion(nextVersion);
        asset.setIsLatest(1);
        asset.setUpdatedAt(LocalDateTime.now());
        assetMapper.updateById(asset);
        return version;
    }

    @Transactional
    public AssetVO rollbackToVersion(Long assetId, Integer targetVersion, Long userId) {
        AssetInfo asset = assetMapper.selectById(assetId);
        if (asset == null) throw new BusinessException("资产不存在");
        if (!asset.getCreatedBy().equals(userId))
            throw new BusinessException(403, "只有资产所有者可以回滚");
        AssetVersion version = versionMapper.selectOne(
            new LambdaQueryWrapper<AssetVersion>()
                .eq(AssetVersion::getAssetId, assetId)
                .eq(AssetVersion::getVersionNumber, targetVersion));
        if (version == null) throw new BusinessException("目标版本不存在");

        asset.setVersion(targetVersion);
        asset.setUpdatedAt(LocalDateTime.now());
        assetMapper.updateById(asset);
        return toVO(asset);
    }

    public Map<String, Object> diffVersions(Long assetId, Integer v1, Integer v2) {
        AssetVersion ver1 = versionMapper.selectOne(
            new LambdaQueryWrapper<AssetVersion>()
                .eq(AssetVersion::getAssetId, assetId).eq(AssetVersion::getVersionNumber, v1));
        AssetVersion ver2 = versionMapper.selectOne(
            new LambdaQueryWrapper<AssetVersion>()
                .eq(AssetVersion::getAssetId, assetId).eq(AssetVersion::getVersionNumber, v2));
        if (ver1 == null || ver2 == null) throw new BusinessException("版本不存在");

        Map<String, Object> diff = new HashMap<>();
        diff.put("assetId", assetId);
        diff.put("v1", toVerMap(ver1));
        diff.put("v2", toVerMap(ver2));
        return diff;
    }

    private Map<String, Object> toVerMap(AssetVersion v) {
        Map<String, Object> m = new HashMap<>();
        m.put("versionNumber", v.getVersionNumber());
        m.put("changeLog", v.getChangeLog() != null ? v.getChangeLog() : "");
        m.put("createdAt", v.getCreatedAt() != null ? v.getCreatedAt().toString() : "");
        return m;
    }

    @Transactional
    public AssetVO uploadFile(MultipartFile file, String name, String type, String ownerType, Long ownerId, Long userId) {
        String originalName = file.getOriginalFilename();
        String assetName = (name != null && !name.isEmpty()) ? name : (originalName != null ? originalName : "Untitled");
        String assetType = (type != null && !type.isEmpty()) ? type : detectType(originalName);

        AssetInfo asset = new AssetInfo();
        asset.setName(assetName);
        asset.setType(assetType);
        asset.setOwnerType(ownerType);
        asset.setOwnerId(ownerId != null ? ownerId : userId);
        asset.setStatus("UPLOADING");
        asset.setVersion(1);
        asset.setIsLatest(1);
        asset.setCreatedBy(userId);
        asset.setAssetCode("AST_" + System.currentTimeMillis());
        asset.setFileFormat(getExtension(originalName));
        asset.setFileSize(file.getSize());
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        assetMapper.insert(asset);

        String objectName = fileStorageService.uploadFile(file, asset.getAssetCode());
        asset.setFileUrl(objectName);
        asset.setStatus("READY");
        assetMapper.updateById(asset);

        return toVO(asset);
    }

    @Transactional
    public AssetVO uploadNewVersion(Long assetId, MultipartFile file, Long userId) {
        AssetInfo asset = assetMapper.selectById(assetId);
        if (asset == null) throw new BusinessException("资产不存在");
        if (!asset.getCreatedBy().equals(userId))
            throw new BusinessException(403, "只有资产所有者可以上传新版本");

        String objectName = fileStorageService.uploadFile(file, asset.getAssetCode());
        int nextVersion = asset.getVersion() + 1;

        AssetVersion version = new AssetVersion();
        version.setAssetId(assetId);
        version.setVersionNumber(nextVersion);
        version.setChangeLog("Uploaded new file: " + file.getOriginalFilename());
        version.setCreatedBy(userId);
        version.setCreatedAt(LocalDateTime.now());
        versionMapper.insert(version);

        asset.setVersion(nextVersion);
        asset.setFileUrl(objectName);
        asset.setFileSize(file.getSize());
        asset.setFileFormat(getExtension(file.getOriginalFilename()));
        asset.setStatus("READY");
        asset.setUpdatedAt(LocalDateTime.now());
        assetMapper.updateById(asset);

        return toVO(asset);
    }

    private String detectType(String fileName) {
        if (fileName == null) return "OTHER";
        String lower = fileName.toLowerCase();
        if (lower.matches(".*\\.(mp4|mov|avi|mkv|webm|flv|wmv)$")) return "VIDEO";
        if (lower.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp|svg)$")) return "IMAGE";
        if (lower.matches(".*\\.(mp3|wav|flac|aac|ogg|wma)$")) return "AUDIO";
        return "OTHER";
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private AssetVO toVO(AssetInfo a) {
        if (a == null) return null;
        AssetVO vo = new AssetVO();
        vo.setId(String.valueOf(a.getId()));
        vo.setAssetCode(a.getAssetCode());
        vo.setName(a.getName());
        vo.setType(a.getType());
        vo.setOwnerType(a.getOwnerType());
        vo.setOwnerId(String.valueOf(a.getOwnerId()));
        vo.setVersion(a.getVersion());
        vo.setStatus(a.getStatus());
        vo.setFileUrl(a.getFileUrl());
        vo.setFileSize(a.getFileSize());
        vo.setFileFormat(a.getFileFormat());
        vo.setDuration(a.getDuration());
        vo.setCreateTime(a.getCreatedAt());
        return vo;
    }
}
