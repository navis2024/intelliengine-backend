package com.aigc.intelliengine.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMITER_SCRIPT =
            "local window_start = tonumber(ARGV[1]); " +
            "local current_time = tonumber(ARGV[2]); " +
            "local window_size = tonumber(ARGV[3]); " +
            "local max_requests = tonumber(ARGV[4]); " +
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, window_start); " +
            "local current_count = redis.call('ZCARD', KEYS[1]); " +
            "if current_count < max_requests then " +
            "    redis.call('ZADD', KEYS[1], current_time, current_time); " +
            "    redis.call('PEXPIRE', KEYS[1], window_size); " +
            "    return 1; " +
            "else " +
            "    return 0; " +
            "end";

    public boolean tryAcquire(String key, int maxRequests, int windowSeconds) {
        long windowMillis = windowSeconds * 1000L;
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - windowMillis;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RATE_LIMITER_SCRIPT);
        redisScript.setResultType(Long.class);

        try {
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
                log.warn("Rate limited: key={}, max={}, window={}s", key, maxRequests, windowSeconds);
            }
            return allowed;
        } catch (Exception e) {
            log.warn("Rate limiter degraded (allow): key={}", key, e);
            return true;
        }
    }
}
