package com.github.liyibo1110.resilience4j.bulkhead.internal;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadConfig;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadFullException;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.EventProcessor;
import com.github.liyibo1110.resilience4j.core.EventPublisher;
import com.github.liyibo1110.resilience4j.core.exception.AcquirePermissionCancelledException;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * 基于semaphore的Bulkhead实现
 * @author liyibo
 * @date 2026-02-09 10:11
 */
public class SemaphoreBulkhead implements Bulkhead {
    private static final String CONFIG_MUST_NOT_BE_NULL = "Config must not be null";
    private static final String TAGS_MUST_NOTE_BE_NULL = "Tags must not be null";

    private final String name;
    private final Semaphore semaphore;
    private final BulkheadMetrics metrics;
    private final BulkheadEventProcessor eventProcessor;

    private final Object configChangesLock = new Object();

    private final Map<String, String> tags;

    /** 注意这个BulkheadConfig是不可变的，每次对其字段修改，都会生成新的实例 */
    private volatile BulkheadConfig config;

    public SemaphoreBulkhead(String name, @Nullable BulkheadConfig bulkheadConfig) {
        this(name, bulkheadConfig, HashMap.empty());
    }

    public SemaphoreBulkhead(String name, @Nullable BulkheadConfig bulkheadConfig,
                             Map<String, String> tags) {
        this.name = name;
        this.config = requireNonNull(bulkheadConfig, CONFIG_MUST_NOT_BE_NULL);
        this.tags = requireNonNull(tags, TAGS_MUST_NOTE_BE_NULL);
        this.semaphore = new Semaphore(config.getMaxConcurrentCalls(), config.isFairCallHandlingEnabled());
        this.metrics = new BulkheadMetrics();
        this.eventProcessor = new BulkheadEventProcessor();
    }

    public SemaphoreBulkhead(String name) {
        this(name, BulkheadConfig.ofDefaults(), HashMap.empty());
    }

    public SemaphoreBulkhead(String name, Supplier<BulkheadConfig> configSupplier) {
        this(name, configSupplier.get(), HashMap.empty());
    }

    public SemaphoreBulkhead(String name, Supplier<BulkheadConfig> configSupplier, Map<String, String> tags) {
        this(name, configSupplier.get(), tags);
    }

    @Override
    public void changeConfig(final BulkheadConfig newConfig) {
        synchronized(configChangesLock) {
            // 先调整semaphore的容量
            int delta = newConfig.getMaxConcurrentCalls() - config.getMaxConcurrentCalls();
            if(delta < 0)   // 减少总配额
                semaphore.acquireUninterruptibly(-delta);
            else if(delta > 0)
                semaphore.release(delta);   // 增加总配额
            config = newConfig;
        }
    }

    @Override
    public boolean tryAcquirePermission() {
        boolean callPermitted = this.tryEnterBulkhead();
        this.publishBulkheadEvent(() -> callPermitted ? new BulkheadOnCallPermittedEvent(name)
                                                      : new BulkheadOnCallRejectedEvent(name));
        return callPermitted;
    }

    @Override
    public void acquirePermission() {
        boolean permitted = tryAcquirePermission();
        if(permitted)   // 成功获取则直接返回即可
            return;
        if(Thread.currentThread().isInterrupted())
            throw new AcquirePermissionCancelledException();
        // 否则抛出特定异常
        throw BulkheadFullException.createBulkheadFullException(this);
    }

    @Override
    public void releasePermission() {
        semaphore.release();
    }

    @Override
    public void onComplete() {
        semaphore.release();
        publishBulkheadEvent(() -> new BulkheadOnCallFinishedEvent(name));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BulkheadConfig getBulkheadConfig() {
        return config;
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public Bulkhead.EventPublisher getEventPublisher() {
        return eventProcessor;
    }

    @Override
    public String toString() {
        return String.format("Bulkhead '%s'", this.name);
    }

    boolean tryEnterBulkhead() {
        long timeout = config.getMaxWaitDuration().toMillis();
        try {
            return semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void publishBulkheadEvent(Supplier<BulkheadEvent> eventSupplier) {
        if(eventProcessor.hasConsumers())
            eventProcessor.consumeEvent(eventSupplier.get());
    }

    private class BulkheadEventProcessor extends EventProcessor<BulkheadEvent>
            implements Bulkhead.EventPublisher, EventConsumer<BulkheadEvent> {

        @Override
        public Bulkhead.EventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallRejectedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public Bulkhead.EventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallPermittedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public Bulkhead.EventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallFinishedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public void consumeEvent(BulkheadEvent event) {
            super.processEvent(event);
        }
    }

    private final class BulkheadMetrics implements Bulkhead.Metrics {

        private BulkheadMetrics() {}

        @Override
        public int getAvailableConcurrentCalls() {
            return semaphore.availablePermits();
        }

        @Override
        public int getMaxAllowedConcurrentCalls() {
            return config.getMaxConcurrentCalls();
        }
    }
}
