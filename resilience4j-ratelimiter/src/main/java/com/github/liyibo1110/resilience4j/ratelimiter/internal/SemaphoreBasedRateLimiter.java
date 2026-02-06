package com.github.liyibo1110.resilience4j.ratelimiter.internal;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;
import io.vavr.collection.Map;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 由Semaphore和Scheduler组成的RateLimiter实现，将在每次limitRefreshPeriod之后刷新权限。
 * @author liyibo
 * @date 2026-02-06 23:32
 */
public class SemaphoreBasedRateLimiter implements RateLimiter {
    private static final String NAME_MUST_NOT_BE_NULL = "Name must not be null";
    private static final String CONFIG_MUST_NOT_BE_NULL = "Config must not be null";

    private final String name;
    private final AtomicReference<RateLimiterConfig> rateLimiterConfig;
    private final ScheduledExecutorService scheduler;
    private final Semaphore semaphore;
    private final SemaphoreBasedRateLimiterMetrics metrics;
    private final Map<String, String> tags;
    private final RateLimiterEventProcessor eventProcessor;

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

    private final class SemaphoreBasedRateLimiterMetrics implements Metrics {
        private SemaphoreBasedRateLimiterMetrics() {}

        @Override
        public int getNumberOfWaitingThreads() {
            return semaphore.getQueueLength();
        }

        @Override
        public int getAvailablePermissions() {
            return semaphore.availablePermits();
        }
    }
}
