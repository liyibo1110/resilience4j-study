package com.github.liyibo1110.resilience4j.bulkhead;

import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.functions.OnceConsumer;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 用来控制特定接口的并发数量，
 * 和RateLimiter有些类似，区别在于RateLimiter具有时间的概念，即每个period允许n个并发
 * @author liyibo
 * @date 2026-02-08 20:44
 */
public interface Bulkhead {

    void changeConfig(BulkheadConfig newConfig);

    boolean tryAcquirePermission();

    void acquirePermission();

    void releasePermission();

    void onComplete();

    String getName();

    BulkheadConfig getBulkheadConfig();

    Metrics getMetrics();

    io.vavr.collection.Map<String, String> getTags();

    EventPublisher getEventPublisher();

    interface Metrics{
        /**
         * 返回此时bulkhead可用的并发数量
         */
        int getAvailableConcurrentCalls();

        /**
         * 允许的最大并发数量
         */
        int getMaxAllowedConcurrentCalls();
    }

    interface EventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<BulkheadEvent> {
        EventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer);
        EventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer);
        EventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer);
    }

    /**
     * 用来给原始Future增加装饰Bulkhead的相关功能
     */
    final class BulkheadFuture<T> implements Future<T> {

        /** 原始业务future */
        private final Future<T> future;
        private final OnceConsumer<Bulkhead> onceToBulkhead;

        BulkheadFuture(Bulkhead bulkhead, Future<T> future) {
            Objects.requireNonNull(future, "Non null Future is required to decorate");
            this.future = future;
            this.onceToBulkhead = OnceConsumer.of(bulkhead);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            try {
                return future.get();
            } finally {
                // 获取原始值之后增加装饰过程（回调onComplete方法）
                onceToBulkhead.applyOnce(Bulkhead::onComplete);
            }
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                return future.get(timeout, unit);
            } finally {
                // 获取原始值之后增加装饰过程（回调onComplete方法）
                onceToBulkhead.applyOnce(Bulkhead::onComplete);
            }
        }
    }
}
