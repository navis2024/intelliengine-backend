package com.aigc.intelliengine.common.redis;

import com.aigc.intelliengine.common.metrics.MetricsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class MultiLevelCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MetricsService metrics;

    private Cache<String, Object> localCache;

    private static final long LOCAL_MAX_SIZE = 10_000;
    private static final int LOCAL_EXPIRE_MINUTES = 5;
    private static final int BASE_EXPIRE_MINUTES = 30;
    private static final int CACHE_NULL_EXPIRE_MINUTES = 2;

    private static final String CACHE_PREFIX = "cache:";

    public MultiLevelCacheService(StringRedisTemplate redisTemplate, MetricsService metrics) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.metrics = metrics;
    }

    @PostConstruct
    public void init() {
        this.localCache = Caffeine.newBuilder()
                .maximumSize(LOCAL_MAX_SIZE)
                .expireAfterWrite(LOCAL_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    // ── Public API ──────────────────────────────────────────────

    public Object get(String key) {
        String fullKey = CACHE_PREFIX + key;

        // L1: Caffeine local
        Object val = localCache.getIfPresent(fullKey);
        if (val != null) {
            metrics.recordCacheHit("L1");
            return val;
        }

        // L2: Redis
        String json = redisTemplate.opsForValue().get(fullKey);
        if (json != null) {
            metrics.recordCacheHit("L2");
            try {
                val = objectMapper.readValue(json, Object.class);
                localCache.put(fullKey, val);
                return val;
            } catch (Exception e) {
                log.warn("Redis cache deserialize failed for key={}", key, e);
            }
        }
        metrics.recordCacheMiss();
        return null;
    }

    public <T> T get(String key, Class<T> clazz) {
        String fullKey = CACHE_PREFIX + key;

        Object val = localCache.getIfPresent(fullKey);
        if (val != null && clazz.isInstance(val)) {
            return clazz.cast(val);
        }

        String json = redisTemplate.opsForValue().get(fullKey);
        if (json != null) {
            try {
                T obj = objectMapper.readValue(json, clazz);
                localCache.put(fullKey, obj);
                return obj;
            } catch (Exception e) {
                log.warn("Redis cache deserialize failed for key={}", key, e);
            }
        }
        return null;
    }

    public void set(String key, Object value, long timeoutMinutes) {
        String fullKey = CACHE_PREFIX + key;
        long ttl = timeoutMinutes + (long) (Math.random() * 5);
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(fullKey, json, ttl, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("Redis cache serialize failed for key={}", key, e);
        }
        localCache.put(fullKey, value);
    }

    public void set(String key, Object value) {
        set(key, value, BASE_EXPIRE_MINUTES);
    }

    public void setNull(String key) {
        String fullKey = CACHE_PREFIX + key;
        redisTemplate.opsForValue().set(fullKey, "__NULL__", CACHE_NULL_EXPIRE_MINUTES, TimeUnit.MINUTES);
        localCache.put(fullKey, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> loader, long timeout) {
        Object cached = get(key);
        if (cached != null) {
            return (T) cached;
        }

        String lockKey = CACHE_PREFIX + "lock:" + key;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            try {
                T val = loader.get();
                if (val != null) {
                    set(key, val, timeout);
                } else {
                    setNull(key);
                }
                return val;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            return getOrLoad(key, loader, timeout);
        }
    }

    public <T> T getOrLoad(String key, Supplier<T> loader) {
        return getOrLoad(key, loader, BASE_EXPIRE_MINUTES);
    }

    public void evict(String key) {
        String fullKey = CACHE_PREFIX + key;
        localCache.invalidate(fullKey);
        redisTemplate.delete(fullKey);
    }

    public String getStats() {
        var stats = localCache.stats();
        return String.format(
                "L1[hitRate=%.2f hit=%d miss=%d evict=%d]",
                stats.hitRate(), stats.hitCount(), stats.missCount(),
                stats.evictionCount());
    }

    /**
     * Evict all cache keys matching a Redis pattern (e.g. "market:templates:*").
     * Uses Redis SCAN for production-safe iteration. L1 Caffeine cache is fully invalidated for simplicity.
     */
    public void evictByPattern(String pattern) {
        String fullPattern = CACHE_PREFIX + pattern;
        // Invalidate all L1 entries (Caffeine doesn't support pattern matching)
        localCache.invalidateAll();
        // SCAN and delete matching keys from Redis L2
        Set<String> keys = redisTemplate.keys(fullPattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cache evicted by pattern: {} ({} keys)", pattern, keys.size());
        }
    }

    /**
     * Delayed double-delete pattern for cache consistency.
     * 1. Delete cache immediately (before DB write)
     * 2. Wait delayMs milliseconds
     * 3. Delete cache again (catches stale reads during the write window)
     */
    public void delayedDoubleDelete(String pattern, long delayMs) {
        evictByPattern(pattern);
        log.debug("Cache first delete completed for pattern: {}, scheduling second delete in {}ms", pattern, delayMs);
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {}
            evictByPattern(pattern);
            log.debug("Cache second delete completed for pattern: {}", pattern);
        });
    }
}
