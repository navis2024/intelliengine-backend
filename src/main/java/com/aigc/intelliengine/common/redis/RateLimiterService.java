package com.aigc.intelliengine.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

/**
 * 接口限流服务
 * <p>
 * 基于Redis实现滑动窗口限流算法
 * 用于防止接口被恶意刷请求，保护系统稳定性
 * <p>
 * 使用场景：
 * 1. 登录接口 - 防止暴力破解
 * 2. 注册接口 - 防止批量注册
 * 3. 敏感操作 - 防止频繁操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Lua脚本：滑动窗口限流
    // KEYS[1]: 限流Key
    // ARGV[1]: 窗口开始时间（毫秒）
    // ARGV[2]: 当前时间（毫秒）
    // ARGV[3]: 窗口大小（毫秒）
    // ARGV[4]: 最大请求数
    private static final String RATE_LIMITER_SCRIPT =
            "local window_start = tonumber(ARGV[1]); " +
            "local current_time = tonumber(ARGV[2]); " +
            "local window_size = tonumber(ARGV[3]); " +
            "local max_requests = tonumber(ARGV[4]); " +
            "local key = KEYS[1]; " +
            "redis.call('ZREMRANGEBYSCORE', key, 0, window_start); " +
            "local current_count = redis.call('ZCARD', key); " +
            "if current_count < max_requests then " +
            "    redis.call('ZADD', key, current_time, current_time); " +
            "    redis.call('PEXPIRE', key, window_size); " +
            "    return 1; " +
            "else " +
            "    return 0; " +
            "end";

    /**
     * 尝试获取限流许可
     * <p>
     * 使用滑动窗口算法，保证在任意时间窗口内的请求数不超过限制
     *
     * @param key        限流Key（如：user:login:{ip}）
     * @param maxRequests 窗口内最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @return true-允许请求，false-请求被限流
     */
    public boolean tryAcquire(String key, int maxRequests, int windowSeconds) {
        long windowMillis = windowSeconds * 1000L;
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - windowMillis;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RATE_LIMITER_SCRIPT);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(windowStart),
                String.valueOf(currentTime),
                String.valueOf(windowMillis),
                String.valueOf(maxRequests)
        );

        boolean allowed = result != null && result == 1;
        if (!allowed) {
            log.warn("请求被限流，key: {}, maxRequests: {}, window: {}s", key, maxRequests, windowSeconds);
        }
        return allowed;
    }

    /**
     * 检查是否允许请求（不带副作用）
     *
     * @param key           限流Key
     * @param maxRequests   最大请求数
     * @param windowSeconds 时间窗口
     * @return true-允许请求
     */
    public boolean checkAllow(String key, int maxRequests, int windowSeconds) {
        long windowMillis = windowSeconds * 1000L;
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - windowMillis;

        // 清理过期数据
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 获取当前窗口内请求数
        Long count = redisTemplate.opsForZSet().zCard(key);
        return count != null && count < maxRequests;
    }

    /**
     * 获取剩余可请求次数
     *
     * @param key           限流Key
     * @param maxRequests   最大请求数
     * @param windowSeconds 时间窗口
     * @return 剩余次数
     */
    public long getRemaining(String key, int maxRequests, int windowSeconds) {
        long windowMillis = windowSeconds * 1000L;
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - windowMillis;

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        Long count = redisTemplate.opsForZSet().zCard(key);

        return Math.max(0, maxRequests - (count != null ? count : 0));
    }
}
