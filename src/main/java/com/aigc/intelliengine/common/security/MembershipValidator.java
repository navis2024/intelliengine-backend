package com.aigc.intelliengine.common.security;

import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.project.ProjectMemberMapper;
import com.aigc.intelliengine.project.model.entity.ProjectInfo;
import com.aigc.intelliengine.project.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipValidator {

    private final ProjectMemberMapper memberMapper;
    private final AssetMapper assetMapper;
    private final ProjectMapper projectMapper;

    public void requireMembership(Long projectId, Long userId) {
        if (projectId == null || userId == null)
            throw new BusinessException("参数错误");
        if (!memberMapper.existsByProjectAndUser(projectId, userId))
            throw new BusinessException("您不是该项目的成员，无法访问");
    }

    public void requireProjectOwner(Long projectId, Long userId) {
        requireMembership(projectId, userId);
        if (!memberMapper.isOwner(projectId, userId))
            throw new BusinessException("仅项目所有者可执行此操作");
    }

    public void requireAssetOwnership(Long assetId, Long userId) {
        AssetInfo asset = assetMapper.selectById(assetId);
        if (asset == null)
            throw new BusinessException("资产不存在");
        if (!userId.equals(asset.getCreatedBy()))
            throw new BusinessException("您不是该资产的所有者");
    }

    public AssetInfo requireAssetAccess(Long assetId, Long userId) {
        AssetInfo asset = assetMapper.selectById(assetId);
        if (asset == null)
            throw new BusinessException("资产不存在");
        // Owner always has access
        if (userId.equals(asset.getCreatedBy()))
            return asset;
        // If asset belongs to a project, check membership
        if ("PROJECT".equals(asset.getOwnerType()) && asset.getOwnerId() != null) {
            requireMembership(asset.getOwnerId(), userId);
            return asset;
        }
        // USER-owned and not the creator
        throw new BusinessException("无权访问该资产");
    }

    public void requireProjectExists(Long projectId) {
        ProjectInfo project = projectMapper.selectById(projectId);
        if (project == null)
            throw new BusinessException("项目不存在");
    }
}
