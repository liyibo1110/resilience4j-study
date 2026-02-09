package com.github.liyibo1110.resilience4j.bulkhead;

import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import com.github.liyibo1110.resilience4j.bulkhead.internal.SemaphoreBulkhead;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.functions.OnceConsumer;
import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedRunnable;
import io.vavr.collection.HashMap;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 用来控制特定接口的并发数量，
 * 和RateLimiter有些类似，区别在于RateLimiter具有时间的概念，即每个period允许n个并发
 * @author liyibo
 * @date 2026-02-08 20:44
 */
public interface Bulkhead {

    static <T> CheckedFunction0<T> decorateCheckedSupplier(Bulkhead bulkhead, CheckedFunction0<T> supplier) {
        return () -> {
            bulkhead.acquirePermission();
            try {
                return supplier.apply();
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T> Supplier<CompletionStage<T>> decorateCompletionStage(Bulkhead bulkhead, Supplier<CompletionStage<T>> supplier) {
        return () -> {
            final CompletableFuture<T> promise = new CompletableFuture<>();
            if(!bulkhead.tryAcquirePermission()) {
                promise.completeExceptionally(BulkheadFullException.createBulkheadFullException(bulkhead));
            }else {
                try {
                    supplier.get().whenComplete((result, t) -> {
                        bulkhead.onComplete();
                        if(t == null)
                            promise.complete(result);
                        else
                            promise.completeExceptionally(t);
                    });
                } catch (Throwable t) {
                    bulkhead.onComplete();
                    promise.completeExceptionally(t);
                }
            }
            return promise;
        };
    }

    static <T> Supplier<Future<T>> decorateFuture(Bulkhead bulkhead, Supplier<Future<T>> supplier) {
        return () -> {
            if(!bulkhead.tryAcquirePermission()) {
                final CompletableFuture<T> promise = new CompletableFuture<>();
                promise.completeExceptionally(BulkheadFullException.createBulkheadFullException(bulkhead));
                return promise;
            }
            try {
                return new BulkheadFuture<>(bulkhead, supplier.get());
            } catch (Throwable e) {
                bulkhead.onComplete();
                throw e;
            }
        };
    }

    static CheckedRunnable decorateCheckedRunnable(Bulkhead bulkhead, CheckedRunnable runnable) {
        return () -> {
            bulkhead.acquirePermission();
            try {
                runnable.run();
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T> Callable<T> decorateCallable(Bulkhead bulkhead, Callable<T> callable) {
        return () -> {
            bulkhead.acquirePermission();
            try {
                return callable.call();
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T> Supplier<T> decorateSupplier(Bulkhead bulkhead, Supplier<T> supplier) {
        return () -> {
            bulkhead.acquirePermission();
            try {
                return supplier.get();
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T> Supplier<Try<T>> decorateTrySupplier(Bulkhead bulkhead, Supplier<Try<T>> supplier) {
        return () -> {
            if(bulkhead.tryAcquirePermission()) {
                try {
                    return supplier.get();
                } finally {
                    bulkhead.onComplete();
                }
            }else {
                return Try.failure(BulkheadFullException.createBulkheadFullException(bulkhead));
            }
        };
    }

    static <T> Consumer<T> decorateConsumer(Bulkhead bulkhead, Consumer<T> consumer) {
        return t -> {
            bulkhead.acquirePermission();
            try {
                consumer.accept(t);
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T> CheckedConsumer<T> decorateCheckedConsumer(Bulkhead bulkhead, CheckedConsumer<T> consumer) {
        return t -> {
            bulkhead.acquirePermission();
            try {
                consumer.accept(t);
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static Runnable decorateRunnable(Bulkhead bulkhead, Runnable runnable) {
        return () -> {
            bulkhead.acquirePermission();
            try {
                runnable.run();
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T, R> Function<T, R> decorateFunction(Bulkhead bulkhead, Function<T, R> function) {
        return (T t) -> {
            bulkhead.acquirePermission();
            try {
                return function.apply(t);
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T, R> CheckedFunction1<T, R> decorateCheckedFunction(Bulkhead bulkhead, CheckedFunction1<T, R> function) {
        return (T t) -> {
            bulkhead.acquirePermission();
            try {
                return function.apply(t);
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static Bulkhead ofDefaults(String name) {
        return new SemaphoreBulkhead(name);
    }

    static Bulkhead of(String name, BulkheadConfig config) {
        return of(name, config, HashMap.empty());
    }

    static Bulkhead of(String name, BulkheadConfig config,
                       io.vavr.collection.Map<String, String> tags) {
        return new SemaphoreBulkhead(name, config, tags);
    }

    static <T> Supplier<Either<Exception, T>> decorateEitherSupplier(Bulkhead bulkhead, Supplier<Either<? extends Exception, T>> supplier) {
        return () -> {
            if(bulkhead.tryAcquirePermission()) {
                try {
                    Either<? extends Exception, T> result = supplier.get();
                    return Either.narrow(result);
                } finally {
                    bulkhead.onComplete();
                }
            }else {
                return Either.left(BulkheadFullException.createBulkheadFullException(bulkhead));
            }
        };
    }

    static Bulkhead of(String name, Supplier<BulkheadConfig> configSupplier) {
        return of(name, configSupplier, HashMap.empty());
    }

    static Bulkhead of(String name, Supplier<BulkheadConfig> configSupplier,
                       io.vavr.collection.Map<String, String> tags) {
        return new SemaphoreBulkhead(name, configSupplier, tags);
    }

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

    default <T> T executeSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier).get();
    }

    default <T> Try<T> executeTrySupplier(Supplier<Try<T>> supplier) {
        return decorateTrySupplier(this, supplier).get();
    }

    default <T> Either<Exception, T> executeEitherSupplier(Supplier<Either<? extends Exception, T>> supplier) {
        return decorateEitherSupplier(this, supplier).get();
    }

    default <T> T executeCallable(Callable<T> callable) throws Exception {
        return decorateCallable(this, callable).call();
    }

    default void executeRunnable(Runnable runnable) {
        decorateRunnable(this, runnable).run();
    }

    default <T> T executeCheckedSupplier(CheckedFunction0<T> checkedSupplier) throws Throwable {
        return decorateCheckedSupplier(this, checkedSupplier).apply();
    }

    default <T> CompletionStage<T> executeCompletionStage(Supplier<CompletionStage<T>> supplier) {
        return decorateCompletionStage(this, supplier).get();
    }

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
