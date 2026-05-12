package com.aigc.intelliengine.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务 — 基于Redisson RLock.
 *
 * 相比手写Redis+Lua脚本的优势:
 *   1. Watch Dog自动续期 — 不会因为业务超时而锁提前释放
 *   2. 可重入 — 同一线程可多次获取同一把锁
 *   3. 公平锁/读写锁/联锁 — 多种锁实现
 *   4. RedLock — 多节点容错
 */
@Slf4j
@Service
public class DistributedLockService {

    private final RedissonClient redisson;
    private static final long DEFAULT_LEASE_SECONDS = 30;

    public DistributedLockService(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public boolean tryLock(String key, long leaseTimeSeconds) {
        RLock lock = redisson.getLock(key);
        try {
            return lock.tryLock(0, leaseTimeSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean tryLockWithWait(String key, long leaseTimeSeconds, long waitTimeMs) throws InterruptedException {
        RLock lock = redisson.getLock(key);
        return lock.tryLock(waitTimeMs, leaseTimeSeconds, TimeUnit.MILLISECONDS);
    }

    public void unlock(String key) {
        RLock lock = redisson.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        } else {
            log.warn("Unlock called but lock not held by current thread: {}", key);
        }
    }

    /**
     * 自动续期模式 — Watch Dog每10秒续期，适合不确定执行时长的任务
     */
    public boolean tryLockWithWatchDog(String key) {
        RLock lock = redisson.getLock(key);
        try {
            return lock.tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public <T> T executeWithLock(String key, Supplier<T> task) {
        RLock lock = redisson.getLock(key);
        try {
            if (!lock.tryLock(0, DEFAULT_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new RuntimeException("操作太频繁，请稍后重试");
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("锁等待被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public <T> T executeWithLock(String key, long leaseTimeSeconds, Supplier<T> task) {
        RLock lock = redisson.getLock(key);
        try {
            if (!lock.tryLock(0, leaseTimeSeconds, TimeUnit.SECONDS)) {
                throw new RuntimeException("操作太频繁，请稍后重试");
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("锁等待被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
