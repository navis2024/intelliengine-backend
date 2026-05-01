package com.aigc.intelliengine.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 * <p>
 * 使用Redis存储已登出/失效的JWT Token
 * 实现用户主动登出功能，防止Token在过期前被继续使用
 * <p>
 * Redis Key格式: token:blacklist:{token}
 * 过期时间: 与Token剩余有效期一致
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Token黑名单Key前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 将Token加入黑名单
     * <p>
     * 用户登出时调用，Token将被标记为失效
     *
     * @param token     JWT Token
     * @param timeout   过期时间（秒）
     */
    public void addToBlacklist(String token, long timeout) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "logout", timeout, TimeUnit.SECONDS);
        log.info("Token已加入黑名单，将在{}秒后过期", timeout);
    }

    /**
     * 检查Token是否在黑名单中
     * <p>
     * JWT过滤器中调用，验证Token是否有效
     *
     * @param token JWT Token
     * @return true-Token在黑名单中（已失效），false-Token有效
     */
    public boolean isBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 从黑名单中移除Token（一般不需要）
     *
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.info("Token已从黑名单移除");
    }
}
