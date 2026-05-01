package com.aigc.intelliengine.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 通用缓存服务
 * <p>
 * 提供基于Redis的缓存操作，用于缓存热点数据
 * <p>
 * 缓存策略：
 * 1. 用户信息缓存 - 减少数据库查询
 * 2. 项目信息缓存 - 加速项目详情加载
 * 3. 字典数据缓存 - 系统配置等不常变数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存Key前缀
    private static final String USER_CACHE_PREFIX = "cache:user:";
    private static final String PROJECT_CACHE_PREFIX = "cache:project:";
    private static final String DICT_CACHE_PREFIX = "cache:dict:";

    /**
     * 缓存用户信息
     *
     * @param userId 用户ID
     * @param user   用户对象
     * @param timeout 过期时间（分钟）
     */
    public void cacheUser(String userId, Object user, long timeout) {
        String key = USER_CACHE_PREFIX + userId;
        redisTemplate.opsForValue().set(key, user, timeout, TimeUnit.MINUTES);
        log.debug("用户信息已缓存: {}", userId);
    }

    /**
     * 获取缓存的用户信息
     *
     * @param userId 用户ID
     * @return 用户对象，不存在返回null
     */
    public Object getCachedUser(String userId) {
        String key = USER_CACHE_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除用户缓存
     *
     * @param userId 用户ID
     */
    public void evictUserCache(String userId) {
        String key = USER_CACHE_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("用户缓存已清除: {}", userId);
    }

    /**
     * 缓存项目信息
     *
     * @param projectId 项目ID
     * @param project   项目对象
     * @param timeout   过期时间（分钟）
     */
    public void cacheProject(String projectId, Object project, long timeout) {
        String key = PROJECT_CACHE_PREFIX + projectId;
        redisTemplate.opsForValue().set(key, project, timeout, TimeUnit.MINUTES);
        log.debug("项目信息已缓存: {}", projectId);
    }

    /**
     * 获取缓存的项目信息
     *
     * @param projectId 项目ID
     * @return 项目对象，不存在返回null
     */
    public Object getCachedProject(String projectId) {
        String key = PROJECT_CACHE_PREFIX + projectId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除项目缓存
     *
     * @param projectId 项目ID
     */
    public void evictProjectCache(String projectId) {
        String key = PROJECT_CACHE_PREFIX + projectId;
        redisTemplate.delete(key);
        log.debug("项目缓存已清除: {}", projectId);
    }

    /**
     * 缓存字典数据
     *
     * @param dictType 字典类型
     * @param data     字典数据
     * @param timeout  过期时间（分钟）
     */
    public void cacheDict(String dictType, Object data, long timeout) {
        String key = DICT_CACHE_PREFIX + dictType;
        redisTemplate.opsForValue().set(key, data, timeout, TimeUnit.MINUTES);
        log.debug("字典数据已缓存: {}", dictType);
    }

    /**
     * 获取缓存的字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据，不存在返回null
     */
    public Object getCachedDict(String dictType) {
        String key = DICT_CACHE_PREFIX + dictType;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 通用缓存设置
     *
     * @param key     缓存Key
     * @param value   缓存值
     * @param timeout 过期时间（分钟）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
    }

    /**
     * 通用缓存获取
     *
     * @param key 缓存Key
     * @return 缓存值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存Key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 设置过期时间
     *
     * @param key     缓存Key
     * @param timeout 过期时间（秒）
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.SECONDS));
    }

    /**
     * 获取过期时间
     *
     * @param key 缓存Key
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
