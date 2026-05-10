package com.aigc.intelliengine.common.controller;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.redis.MultiLevelCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private static final LocalDateTime START_TIME = LocalDateTime.now();
    private final MultiLevelCacheService cacheService;

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "intelliengine-backend");
        status.put("version", "1.0.0");
        status.put("startTime", START_TIME.toString());
        status.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(status);
    }

    @GetMapping("/cache/stats")
    public ApiResponse<Map<String, Object>> cacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheStats", cacheService.getStats());
        return ApiResponse.success(stats);
    }
}
