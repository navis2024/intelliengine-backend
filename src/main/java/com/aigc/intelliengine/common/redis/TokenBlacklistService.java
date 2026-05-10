package com.aigc.intelliengine.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    public void addToBlacklist(String token, long timeout) {
        redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", timeout, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    public void removeFromBlacklist(String token) {
        redisTemplate.delete(TOKEN_BLACKLIST_PREFIX + token);
    }
}
