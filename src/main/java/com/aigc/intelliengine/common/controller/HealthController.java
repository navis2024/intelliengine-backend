package com.aigc.intelliengine.common.controller;

import com.aigc.intelliengine.common.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * <p>
 * 用于 Docker 健康检查和服务状态监控
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final LocalDateTime START_TIME = LocalDateTime.now();

    /**
     * 健康检查
     */
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
}
