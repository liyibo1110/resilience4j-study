package com.github.liyibo1110.resilience4j.ratelimiter.internal;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnDrainedEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnFailureEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnSuccessEvent;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Option;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
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

    public SemaphoreBasedRateLimiter(final String name, final RateLimiterConfig rateLimiterConfig) {
        this(name, rateLimiterConfig, HashMap.empty());
    }

    public SemaphoreBasedRateLimiter(final String name, final RateLimiterConfig rateLimiterConfig,
                                     Map<String, String> tags) {
        this(name, rateLimiterConfig, null, tags);
    }

    public SemaphoreBasedRateLimiter(String name, RateLimiterConfig rateLimiterConfig,
                                     @Nullable ScheduledExecutorService scheduler) {
        this(name, rateLimiterConfig, scheduler, HashMap.empty());
    }

    public SemaphoreBasedRateLimiter(String name, RateLimiterConfig rateLimiterConfig,
                                     @Nullable ScheduledExecutorService scheduler, Map<String, String> tags) {
        this.name = Objects.requireNonNull(name, NAME_MUST_NOT_BE_NULL);
        this.rateLimiterConfig = new AtomicReference<>(Objects.requireNonNull(rateLimiterConfig, CONFIG_MUST_NOT_BE_NULL));
        this.scheduler = Option.of(scheduler).getOrElse(this::configureScheduler);
        this.tags = tags;
        this.semaphore = new Semaphore(this.rateLimiterConfig.get().getLimitForPeriod(), true);
        this.metrics = new SemaphoreBasedRateLimiterMetrics();
        this.eventProcessor = new RateLimiterEventProcessor();
        this.scheduleLimitRefresh();
    }

    /**
     * 生成默认的延迟线程连接池
     */
    private ScheduledExecutorService configureScheduler() {
        ThreadFactory factory = r -> {
            Thread thread = new Thread(r, "SchedulerForSemaphoreBasedRateLimiterImpl-" + name);
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newSingleThreadScheduledExecutor(factory);
    }

    /**
     * 定期运行refreshLimit方法
     */
    private void scheduleLimitRefresh() {
        this.scheduler.scheduleAtFixedRate(
            this::refreshLimit,
            this.rateLimiterConfig.get().getLimitRefreshPeriod().toNanos(),
            this.rateLimiterConfig.get().getLimitRefreshPeriod().toNanos(),
            TimeUnit.NANOSECONDS);
    }

    /**
     * 重置许可总数（在scheduler里定期运行）
     */
    void refreshLimit() {
        int permissionsToRelease = this.rateLimiterConfig.get().getLimitForPeriod() - this.semaphore.availablePermits();
        this.semaphore.release(permissionsToRelease);
    }

    @Override
    public void changeTimeoutDuration(Duration timeoutDuration) {
        RateLimiterConfig newConfig = RateLimiterConfig.from(this.rateLimiterConfig.get())
                .timeoutDuration(timeoutDuration)
                .build();
        this.rateLimiterConfig.set(newConfig);
    }

    @Override
    public void changeLimitForPeriod(int limitForPeriod) {
        RateLimiterConfig newConfig = RateLimiterConfig.from(this.rateLimiterConfig.get())
                .limitForPeriod(limitForPeriod)
                .build();
        this.rateLimiterConfig.set(newConfig);
    }

    /**
     * 核心方法：尝试获取n个许可，注意方法只会返回bool来表示获取是否成功
     */
    @Override
    public boolean acquirePermission(int permits) {
        try {
            boolean success = this.semaphore.tryAcquire(permits, rateLimiterConfig.get().getTimeoutDuration().toNanos(), TimeUnit.NANOSECONDS);
            this.publishRateLimiterAcquisitionEvent(success, permits);
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.publishRateLimiterAcquisitionEvent(false, permits);
            return false;
        }
    }

    @Override
    public long reservePermission() {
        throw new UnsupportedOperationException("Reserving permissions is not supported in the semaphore based implementation");
    }

    @Override
    public long reservePermission(int permits) {
        throw new UnsupportedOperationException("Reserving permissions is not supported in the semaphore based implementation");
    }

    @Override
    public void drainPermissions() {
        // 还是利用了semaphore自身的drainAPI
        int permits = this.semaphore.drainPermits();
        if(this.eventProcessor.hasConsumers())
            this.eventProcessor.consumeEvent(new RateLimiterOnDrainedEvent(this.name, permits));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Metrics getMetrics() {
        return this.metrics;
    }

    @Override
    public EventPublisher getEventPublisher() {
        return this.eventProcessor;
    }

    @Override
    public RateLimiterConfig getRateLimiterConfig() {
        return this.rateLimiterConfig.get();
    }

    @Override
    public String toString() {
        return "SemaphoreBasedRateLimiter{"
                + "name='" + name + '\''
                + ", rateLimiterConfig=" + rateLimiterConfig
                + '}';
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    private void publishRateLimiterAcquisitionEvent(boolean permissionAcquired, int permits) {
        if(!this.eventProcessor.hasConsumers())
            return;
        if(permissionAcquired) {
            this.eventProcessor.consumeEvent(new RateLimiterOnSuccessEvent(this.name, permits));
            return;
        }
        this.eventProcessor.consumeEvent(new RateLimiterOnFailureEvent(this.name, permits));
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
