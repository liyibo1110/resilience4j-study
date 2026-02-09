package com.github.liyibo1110.resilience4j.bulkhead;

import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.internal.FixedThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import io.vavr.collection.Map;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * 基于线程池的Bulkhead
 * @author liyibo
 * @date 2026-02-09 10:48
 */
public interface ThreadPoolBulkhead extends AutoCloseable {

    static <T> Supplier<CompletionStage<T>> decorateCallable(ThreadPoolBulkhead bulkhead, Callable<T> callable) {
        return () -> bulkhead.submit(callable);
    }

    static <T> Supplier<CompletionStage<T>> decorateSupplier(ThreadPoolBulkhead bulkhead, Supplier<T> supplier) {
        return () -> bulkhead.submit(supplier::get);
    }

    static Supplier<CompletionStage<Void>> decorateRunnable(ThreadPoolBulkhead bulkhead, Runnable runnable) {
        return () -> bulkhead.submit(runnable);
    }

    static ThreadPoolBulkhead of(String name, ThreadPoolBulkheadConfig config) {
        return new FixedThreadPoolBulkhead(name, config);
    }

    static ThreadPoolBulkhead ofDefaults(String name) {
        return new FixedThreadPoolBulkhead(name);
    }

    static ThreadPoolBulkhead of(String name, ThreadPoolBulkheadConfig config, io.vavr.collection.Map<String, String> tags) {
        return new FixedThreadPoolBulkhead(name, config, tags);
    }

    static ThreadPoolBulkhead of(String name, Supplier<ThreadPoolBulkheadConfig> configSupplier) {
        return new FixedThreadPoolBulkhead(name, configSupplier);
    }

    <T> CompletionStage<T> submit(Callable<T> task);

    CompletionStage<Void> submit(Runnable task);

    String getName();

    ThreadPoolBulkheadConfig getBulkheadConfig();

    Metrics getMetrics();

    Map<String, String> getTags();

    ThreadPoolBulkheadEventPublisher getEventPublisher();

    default <T> Supplier<CompletionStage<T>> decorateSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier);
    }

    default <T> Supplier<CompletionStage<T>> decorateCallable(Callable<T> callable) {
        return decorateCallable(this, callable);
    }

    default Supplier<CompletionStage<Void>> decorateRunnable(Runnable runnable) {
        return decorateRunnable(this, runnable);
    }

    default <T> CompletionStage<T> executeSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier).get();
    }

    default <T> CompletionStage<T> executeCallable(Callable<T> callable) {
        return decorateCallable(this, callable).get();
    }

    default CompletionStage<Void> executeRunnable(Runnable runnable) {
        return decorateRunnable(this, runnable).get();
    }

    interface Metrics {
        int getCoreThreadPoolSize();
        int getThreadPoolSize();
        int getMaximumThreadPoolSize();

        /** 队列中的任务数 */
        int getQueueDepth();
        int getRemainingQueueCapacity();
        int getQueueCapacity();
    }

    interface ThreadPoolBulkheadEventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<BulkheadEvent> {
        ThreadPoolBulkheadEventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer);
        ThreadPoolBulkheadEventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer);
        ThreadPoolBulkheadEventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer);
    }
}
