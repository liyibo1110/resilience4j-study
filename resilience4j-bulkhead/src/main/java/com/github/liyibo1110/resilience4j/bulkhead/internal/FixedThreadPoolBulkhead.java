package com.github.liyibo1110.resilience4j.bulkhead.internal;

import com.github.liyibo1110.resilience4j.bulkhead.BulkheadFullException;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import com.github.liyibo1110.resilience4j.core.ContextPropagator;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.EventProcessor;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-09 10:47
 */
public class FixedThreadPoolBulkhead implements ThreadPoolBulkhead {
    private static final String CONFIG_MUST_NOT_BE_NULL = "Config must not be null";
    private static final String TAGS_MUST_NOTE_BE_NULL = "Tags must not be null";

    private final String name;

    /** 内部核心依赖 */
    private final ThreadPoolExecutor executorService;
    private final FixedThreadPoolBulkhead.BulkheadMetrics metrics;
    private final FixedThreadPoolBulkhead.BulkheadEventProcessor eventProcessor;
    private final ThreadPoolBulkheadConfig config;
    private final Map<String, String> tags;

    public FixedThreadPoolBulkhead(String name, @Nullable ThreadPoolBulkheadConfig bulkheadConfig) {
        this(name, bulkheadConfig, HashMap.empty());
    }

    public FixedThreadPoolBulkhead(String name, @Nullable ThreadPoolBulkheadConfig bulkheadConfig,
                                   Map<String, String> tags) {
        this.name = name;
        this.config = requireNonNull(bulkheadConfig, CONFIG_MUST_NOT_BE_NULL);
        this.tags = requireNonNull(tags, TAGS_MUST_NOTE_BE_NULL);
        this.executorService = new ThreadPoolExecutor(config.getCoreThreadPoolSize(),
                config.getMaxThreadPoolSize(),
                config.getKeepAliveDuration().toMillis(), TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(config.getQueueCapacity()),
                new BulkheadNamingThreadFactory(name),
                config.getRejectedExecutionHandler());
        this.metrics = new FixedThreadPoolBulkhead.BulkheadMetrics();
        this.eventProcessor = new FixedThreadPoolBulkhead.BulkheadEventProcessor();
    }

    public FixedThreadPoolBulkhead(String name) {
        this(name, ThreadPoolBulkheadConfig.ofDefaults(), HashMap.empty());
    }

    public FixedThreadPoolBulkhead(String name, Map<String, String> tags) {
        this(name, ThreadPoolBulkheadConfig.ofDefaults(), tags);
    }

    public FixedThreadPoolBulkhead(String name, Supplier<ThreadPoolBulkheadConfig> configSupplier) {
        this(name, configSupplier.get(), HashMap.empty());
    }

    public FixedThreadPoolBulkhead(String name, Supplier<ThreadPoolBulkheadConfig> configSupplier,
                                   Map<String, String> tags) {
        this(name, configSupplier.get(), tags);
    }

    @Override
    public <T> CompletableFuture<T> submit(Callable<T> callable) {
        // 用来保存callable执行的最终结果
        final CompletableFuture<T> promise = new CompletableFuture<>();
        try {
            // 真正的callable会在内置的线程池里面跑，而不是当前用户线程，同时加入了线程上下文传播机制
            CompletableFuture.supplyAsync(ContextPropagator.decorateSupplier(config.getContextPropagator(), () -> {
                // 真正的callable调用增强，如果进来了说明已经可以在线程池中执行了（线程池队列没有满）
                try {
                    publishBulkheadEvent(() -> new BulkheadOnCallPermittedEvent(name));
                    return callable.call();
                } catch (CompletionException e) {
                    throw e;
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }), executorService).whenComplete((result, t) -> {
                publishBulkheadEvent(() -> new BulkheadOnCallFinishedEvent(name));
                if(t == null)
                    promise.complete(result);
                else
                    promise.completeExceptionally(t);
            });
        } catch (RejectedExecutionException rejected) { // 如果内部线程池满了，无法运行任务，则直接进入catch
            publishBulkheadEvent(() -> new BulkheadOnCallRejectedEvent(name));
            throw BulkheadFullException.createBulkheadFullException(this);
        }
        return promise;
    }

    @Override
    public CompletableFuture<Void> submit(Runnable runnable) {
// 用来保存callable执行的最终结果
        final CompletableFuture<Void> promise = new CompletableFuture<>();
        try {
            // 真正的callable会在内置的线程池里面跑，而不是当前用户线程，同时加入了线程上下文传播机制
            CompletableFuture.runAsync(ContextPropagator.decorateRunnable(config.getContextPropagator(), () -> {
                // 真正的callable调用增强，如果进来了说明已经可以在线程池中执行了（线程池队列没有满）
                try {
                    publishBulkheadEvent(() -> new BulkheadOnCallPermittedEvent(name));
                    runnable.run();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }), executorService).whenComplete((result, t) -> {
                publishBulkheadEvent(() -> new BulkheadOnCallFinishedEvent(name));
                if(t == null)
                    promise.complete(result);
                else
                    promise.completeExceptionally(t);
            });
        } catch (RejectedExecutionException rejected) { // 如果内部线程池满了，无法运行任务，则直接进入catch
            publishBulkheadEvent(() -> new BulkheadOnCallRejectedEvent(name));
            throw BulkheadFullException.createBulkheadFullException(this);
        }
        return promise;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ThreadPoolBulkheadConfig getBulkheadConfig() {
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
    public ThreadPoolBulkheadEventPublisher getEventPublisher() {
        return eventProcessor;
    }

    private void publishBulkheadEvent(Supplier<BulkheadEvent> eventSupplier) {
        if(eventProcessor.hasConsumers())
            eventProcessor.consumeEvent(eventSupplier.get());
    }

    @Override
    public String toString() {
        return String.format("FixedThreadPoolBulkhead '%s'", this.name);
    }

    @Override
    public void close() {
        this.executorService.shutdown();
        try {
            if(!this.executorService.awaitTermination(5, TimeUnit.SECONDS));
            this.executorService.shutdownNow();
        } catch (InterruptedException e) {
            if(!this.executorService.isTerminated())
                this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private class BulkheadEventProcessor extends EventProcessor<BulkheadEvent> implements
            ThreadPoolBulkhead.ThreadPoolBulkheadEventPublisher, EventConsumer<BulkheadEvent> {

        @Override
        public ThreadPoolBulkhead.ThreadPoolBulkheadEventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallRejectedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public ThreadPoolBulkhead.ThreadPoolBulkheadEventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallPermittedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public ThreadPoolBulkhead.ThreadPoolBulkheadEventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallFinishedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public void consumeEvent(BulkheadEvent event) {
            super.processEvent(event);
        }
    }

    private final class BulkheadMetrics implements ThreadPoolBulkhead.Metrics {

        private BulkheadMetrics() {}


        @Override
        public int getCoreThreadPoolSize() {
            return executorService.getCorePoolSize();
        }

        @Override
        public int getThreadPoolSize() {
            return executorService.getPoolSize();
        }

        @Override
        public int getMaximumThreadPoolSize() {
            return executorService.getMaximumPoolSize();
        }

        @Override
        public int getQueueDepth() {
            return executorService.getQueue().size();
        }

        @Override
        public int getRemainingQueueCapacity() {
            return executorService.getQueue().remainingCapacity();
        }

        @Override
        public int getQueueCapacity() {
            return config.getQueueCapacity();
        }
    }
}
