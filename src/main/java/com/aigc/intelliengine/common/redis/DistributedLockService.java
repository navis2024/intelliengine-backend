package com.aigc.intelliengine.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;
    private final ThreadLocal<String> lockOwner = new ThreadLocal<>();

    private static final String LOCK_PREFIX = "distlock:";

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]); " +
            "else " +
            "    return 0; " +
            "end";

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, long timeoutSeconds) {
        String lockKey = LOCK_PREFIX + key;
        String owner = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, owner, timeoutSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            lockOwner.set(owner);
            return true;
        }
        return false;
    }

    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        String owner = lockOwner.get();
        if (owner == null) {
            log.warn("Unlock called without lock ownership: {}", key);
            return;
        }
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), owner);
        if (result != null && result == 1) {
            lockOwner.remove();
        } else {
            log.warn("Unlock failed — lock expired or owned by another process: {}", key);
        }
    }

    public boolean tryLockWithWait(String key, long leaseTimeSeconds, long waitTimeMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + waitTimeMs;
        while (System.currentTimeMillis() < deadline) {
            if (tryLock(key, leaseTimeSeconds)) {
                return true;
            }
            Thread.sleep(50);
        }
        return false;
    }

    public <T> T executeWithLock(String key, long timeoutSeconds, java.util.concurrent.Callable<T> task) throws Exception {
        if (!tryLock(key, timeoutSeconds)) {
            throw new RuntimeException("操作太频繁，请稍后重试");
        }
        try {
            return task.call();
        } finally {
            unlock(key);
        }
    }
}
