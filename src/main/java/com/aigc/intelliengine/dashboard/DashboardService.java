package com.aigc.intelliengine.dashboard;

import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.asset.model.vo.AssetVO;
import com.aigc.intelliengine.dashboard.model.vo.DashboardStatsVO;
import com.aigc.intelliengine.project.ProjectMemberMapper;
import com.aigc.intelliengine.project.model.entity.ProjectMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectMemberMapper memberMapper;
    private final AssetMapper assetMapper;

    public DashboardStatsVO getStats(Long userId) {
        List<Long> projectIds = memberMapper.selectByUser(userId).stream()
                .map(ProjectMember::getProjectId).toList();

        long totalProjects = projectIds.size();
        long totalAssets = assetMapper.countByUserAndProjects(userId, projectIds);

        List<AssetInfo> recentAssets = assetMapper.selectRecentByUserAndProjects(userId, projectIds, 5);
        List<AssetVO> recentVOs = recentAssets.stream().map(this::toAssetVO).toList();

        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setTotalProjects(totalProjects);
        vo.setTotalAssets(totalAssets);
        vo.setRecentAssets(recentVOs);
        return vo;
    }

    private AssetVO toAssetVO(AssetInfo a) {
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
