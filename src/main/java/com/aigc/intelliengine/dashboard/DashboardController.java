package com.aigc.intelliengine.dashboard;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.security.UserContextHolder;
import com.aigc.intelliengine.dashboard.model.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "仪表盘统计")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据")
    public ApiResponse<DashboardStatsVO> getStats() {
        return ApiResponse.success(dashboardService.getStats(UserContextHolder.getCurrentUserId()));
    }
}
