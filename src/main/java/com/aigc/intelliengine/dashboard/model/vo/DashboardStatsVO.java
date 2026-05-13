package com.aigc.intelliengine.dashboard.model.vo;

import com.aigc.intelliengine.asset.model.vo.AssetVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Dashboard统计数据")
public class DashboardStatsVO {
    @Schema(description = "项目总数")
    private Long totalProjects;
    @Schema(description = "资产总数")
    private Long totalAssets;
    @Schema(description = "最近资产")
    private List<AssetVO> recentAssets;
}
