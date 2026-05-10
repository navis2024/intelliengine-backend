package com.aigc.intelliengine.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis Bitmap based Bloom Filter — prevents duplicate purchases and other repeat operations.
 *
 * Parameters (tuned for ~10K items, 1% false positive rate):
 *   - bitSize: 95,854  (~12 KB per filter)
 *   - hashCount: 7
 *
 * Uses MurmurHash3-inspired double-hashing technique for k hash functions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBloomFilter {

    private final StringRedisTemplate redisTemplate;

    /** Redis key prefix for bloom filters */
    private static final String BF_PREFIX = "bloom:";

    /** Bitmap size — ~100K bits = ~12KB */
    private static final int BIT_SIZE = 95_854;

    /** Number of hash functions */
    private static final int HASH_COUNT = 7;

    /**
     * Add value to bloom filter.
     * @param filterName logical filter name (e.g. "purchase")
     * @param value value to add
     */
    public void add(String filterName, String value) {
        int[] offsets = hashOffsets(value);
        String key = key(filterName);
        List<Object> args = new ArrayList<>();
        for (int offset : offsets) {
            args.add(String.valueOf(offset));
        }
        // SETBIT for each offset
        for (int offset : offsets) {
            redisTemplate.opsForValue().setBit(key, offset, true);
        }
    }

    /**
     * Check if value MIGHT exist in the filter.
     * @return true if value might exist (could be false positive), false if definitely not
     */
    public boolean mightContain(String filterName, String value) {
        int[] offsets = hashOffsets(value);
        String key = key(filterName);
        for (int offset : offsets) {
            Boolean bit = redisTemplate.opsForValue().getBit(key, offset);
            if (bit == null || !bit) return false;
        }
        return true;
    }

    /**
     * Convenience: check and add in one operation.
     * @return true if value was NEW (not previously seen), false if duplicate
     */
    public boolean checkAndAdd(String filterName, String value) {
        if (mightContain(filterName, value)) {
            log.warn("Bloom filter hit for {}: {}", filterName, value);
            return false;
        }
        add(filterName, value);
        return true;
    }

    private String key(String filterName) {
        return BF_PREFIX + filterName;
    }

    /**
     * Generate k hash offsets using double-hashing technique.
     * h(i) = (h1 + i * h2) % BIT_SIZE
     */
    private int[] hashOffsets(String value) {
        int[] offsets = new int[HASH_COUNT];
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        long h1 = murmur64(bytes, 0x9747b28c);
        long h2 = murmur64(bytes, 0x9e3779b9);
        for (int i = 0; i < HASH_COUNT; i++) {
            long hash = h1 + (long) i * h2;
            offsets[i] = Math.abs((int) (hash % BIT_SIZE));
        }
        return offsets;
    }

    /**
     * 64-bit MurmurHash-like hash (simplified for Bloom filter use).
     */
    private long murmur64(byte[] data, long seed) {
        long m = 0xc6a4a7935bd1e995L;
        int r = 47;
        long h = seed ^ (data.length * m);

        int len = data.length - (data.length % 8);
        for (int i = 0; i < len; i += 8) {
            long k = ((long) data[i] & 0xff)
                    | (((long) data[i + 1] & 0xff) << 8)
                    | (((long) data[i + 2] & 0xff) << 16)
                    | (((long) data[i + 3] & 0xff) << 24)
                    | (((long) data[i + 4] & 0xff) << 32)
                    | (((long) data[i + 5] & 0xff) << 40)
                    | (((long) data[i + 6] & 0xff) << 48)
                    | (((long) data[i + 7] & 0xff) << 56);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h ^= k;
            h *= m;
        }

        int remaining = data.length % 8;
        if (remaining > 0) {
            long tail = 0;
            for (int i = len; i < data.length; i++) {
                tail |= ((long) data[i] & 0xff) << (8 * (i - len));
            }
            h ^= tail;
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;
        return h;
    }
}
