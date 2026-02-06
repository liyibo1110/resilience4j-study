package com.github.liyibo1110.resilience4j.ratelimiter.internal;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;
import io.vavr.collection.Map;

import java.time.Duration;

/**
 * 将从纪元开始以来的所有纳秒数拆分为周期，每个周期持续时间为limitRefreshPeriod纳秒。
 * 根据每个开始的合约，将State.activePermissions设置为limitForPeriod。
 * 所有更新都是原子性的，并且状态被封装在AtomicReference中。
 * @author liyibo
 * @date 2026-02-06 23:33
 */
public class AtomicRateLimiter implements RateLimiter {
    @Override
    public void changeTimeoutDuration(Duration timeoutDuration) {

    }

    @Override
    public void changeLimitForPeriod(int limitForPeriod) {

    }

    @Override
    public boolean acquirePermission(int permits) {
        return false;
    }

    @Override
    public long reservePermission(int permits) {
        return 0;
    }

    @Override
    public void drainPermissions() {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public RateLimiterConfig getRateLimiterConfig() {
        return null;
    }

    @Override
    public Map<String, String> getTags() {
        return null;
    }

    @Override
    public Metrics getMetrics() {
        return null;
    }

    @Override
    public EventPublisher getEventPublisher() {
        return null;
    }
}
