package com.aigc.intelliengine.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 自定义业务指标注册中心.
 *
 * 暴露给Prometheus的指标:
 *   - agent_execution_seconds — Agent任务执行时长(按status)
 *   - cache_gets_total — 缓存访问计数(按result: hit/miss, level: L1/L2)
 *   - rag_search_seconds — RAG检索延迟
 */
@Service
public class MetricsService {

    private final MeterRegistry registry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    // ── Agent metrics ──────────────────────────────────────────

    public void recordAgentExecution(long elapsedMs, String status) {
        timer("agent.execution", "status", status).record(elapsedMs, TimeUnit.MILLISECONDS);
        counter("agent.executions", "status", status).increment();
    }

    // ── Cache metrics ──────────────────────────────────────────

    public void recordCacheHit(String level) {
        counter("cache.gets", "result", "hit", "level", level).increment();
    }

    public void recordCacheMiss() {
        counter("cache.gets", "result", "miss", "level", "L2").increment();
    }

    // ── RAG metrics ────────────────────────────────────────────

    public <T> T recordRagSearch(Supplier<T> search) {
        return timer("rag.search").record(search);
    }

    // ── Internal helpers (memoized) ────────────────────────────

    private Timer timer(String name, String... tags) {
        String key = name + "|" + String.join(",", tags);
        return timers.computeIfAbsent(key, k ->
                Timer.builder(name).tags(tags).register(registry));
    }

    private Counter counter(String name, String... tags) {
        String key = name + "|" + String.join(",", tags);
        return counters.computeIfAbsent(key, k ->
                Counter.builder(name).tags(tags).register(registry));
    }
}
