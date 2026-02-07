package com.github.liyibo1110.resilience4j.ratelimiter.internal;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 将从纪元开始以来的所有纳秒数拆分为周期，每个周期持续时间为limitRefreshPeriod纳秒。
 * 根据每个开始的合约，将State.activePermissions设置为limitForPeriod。
 * 所有更新都是原子性的，并且状态被封装在AtomicReference中。
 * @author liyibo
 * @date 2026-02-06 23:33
 */
public class AtomicRateLimiter implements RateLimiter {
    /** AtomicRateLimiter实例的生成时间点 */
    private final long nanoTimeStart;
    private final String name;

    /** 等候许可的线程总数 */
    private final AtomicInteger waitingThreads;

    /** 内置了状态对象（3个字段） */
    private final AtomicReference<State> state;
    private final Map<String, String> tags;
    private final RateLimiterEventProcessor eventProcessor;

    public AtomicRateLimiter(String name, RateLimiterConfig config) {
        this(name, config, HashMap.empty());
    }

    public AtomicRateLimiter(String name, RateLimiterConfig config, Map<String, String> tags) {
        this.name = name;
        this.tags = tags;
        this.nanoTimeStart = System.nanoTime();
        this.waitingThreads = new AtomicInteger(0);
        this.state = new AtomicReference<>(new State(config, 0, config.getLimitForPeriod(), 0));
        this.eventProcessor = new RateLimiterEventProcessor();
    }

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

    /**
     * 表示这个RateLimiter实现类的状态
     */
    private static class State {
        private final RateLimiterConfig config;

        /** 上一次调用acquirePermission()时，使用的周期数，这个值如果变了，则许可将要恢复重置 */
        private final long activeCycle;

        /** 上一次调用acquirePermission()后，可用的许可数量，如果某些许可被预支，该值可能为负数 */
        private final int activePermissions;

        /** 上一次调用acquirePermission()时，需要等待获取许可的纳秒数 */
        private final long nanosToWait;

        private State(RateLimiterConfig config, final long activeCycle, final int activePermissions, final long nanosToWait) {
            this.config = config;
            this.activeCycle = activeCycle;
            this.activePermissions = activePermissions;
            this.nanosToWait = nanosToWait;
        }
    }

    public class AtomicRateLimiterMetrics implements Metrics {

        private AtomicRateLimiterMetrics() {}

        @Override
        public int getNumberOfWaitingThreads() {
            return waitingThreads.get();
        }

        @Override
        public int getAvailablePermissions() {
            State currentState = state.get();
            return 0;
        }
    }
}
