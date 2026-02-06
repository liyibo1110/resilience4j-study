package com.github.liyibo1110.resilience4j.retry;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.metrics.Metrics;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnErrorEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnIgnoredErrorEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnRetryEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnSuccessEvent;
import com.github.liyibo1110.resilience4j.retry.internal.RetryImpl;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedRunnable;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 就是字面意思的retry功能，类似Spring Retry，整体模块理解难度低于CircuitBreaker，结构都是类似的。
 * @author liyibo
 * @date 2026-02-06 10:29
 */
public interface Retry {

    static Retry of(String name, RetryConfig retryConfig) {
        return of(name, retryConfig, HashMap.empty());
    }

    static Retry of(String name, RetryConfig retryConfig, Map<String, String> tags) {
        return new RetryImpl(name, retryConfig, tags);
    }

    static Retry of(String name, Supplier<RetryConfig> configSupplier) {
        return of(name, configSupplier.get(), HashMap.empty());
    }

    static Retry of(String name, Supplier<RetryConfig> configSupplier,
                    Map<String, String> tags) {
        return new RetryImpl(name, configSupplier.get(), tags);
    }

    static Retry ofDefaults(String name) {
        return of(name, RetryConfig.ofDefaults(), HashMap.empty());
    }

    /**
     * 各种Retry的装饰器，原理都差不多，后面不写注释了，注意但凡异步的call，都要自己负责生成带延迟的线程
     */
    static <T> Supplier<CompletionStage<T>> decorateCompletionStage(Retry retry, ScheduledExecutorService scheduler,
                                                                    Supplier<CompletionStage<T>> supplier) {
        return () -> {
            final CompletableFuture<T> promise = new CompletableFuture<>();
            final Runnable block = new AsyncRetryBlock<>(scheduler, retry.asyncContext(), supplier, promise);
            block.run();
            return promise;
        };
    }

    static <T> CheckedFunction0<T> decorateCheckedSupplier(Retry retry, CheckedFunction0<T> supplier) {
        return () -> {
            Retry.Context<T> context = retry.context();
            do {    // 先执行1次
                try {
                    T result = supplier.apply();
                    final boolean needRetry = context.onResult(result);
                    if(!needRetry) {    // 不需要，则执行onComplete来收尾
                        context.onComplete();
                        return result;
                    }
                } catch (Exception e) {
                    context.onError(e);
                }
            } while(true);
        };
    }

    static CheckedRunnable decorateCheckedRunnable(Retry retry, CheckedRunnable runnable) {
        return () -> {
            Retry.Context context = retry.context();
            do {    // 先执行1次
                try {
                    runnable.run();
                    context.onComplete();
                    break;
                } catch (Exception e) {
                    context.onError(e);
                }
            } while(true);
        };
    }

    static <T, R> CheckedFunction1<T, R> decorateCheckedFunction(Retry retry, CheckedFunction1<T, R> function) {
        return (T t) -> {
            Retry.Context<R> context = retry.context();
            do {    // 先执行1次
                try {
                    R result = function.apply(t);
                    final boolean needRetry = context.onResult(result);
                    if(!needRetry) {    // 不需要，则执行onComplete来收尾
                        context.onComplete();
                        return result;
                    }
                } catch (Exception e) {
                    context.onError(e);
                }
            } while(true);
        };
    }

    static <T> Supplier<T> decorateSupplier(Retry retry, Supplier<T> supplier) {
        return () -> {
            Retry.Context<T> context = retry.context();
            do {    // 先执行1次
                try {
                    T result = supplier.get();
                    final boolean needRetry = context.onResult(result);
                    if(!needRetry) {    // 不需要，则执行onComplete来收尾
                        context.onComplete();
                        return result;
                    }
                } catch (RuntimeException e) {
                    context.onRuntimeError(e);
                }
            } while(true);
        };
    }

    static <E extends Exception, T> Supplier<Either<E, T>> decorateEitherSupplier(Retry retry, Supplier<Either<E, T>> supplier) {
        return () -> {
            Retry.Context<T> context = retry.context();
            do {    // 先执行1次
                Either<E, T> result = supplier.get();
                if(result.isRight()) {
                    final boolean needRetry = context.onResult(result.get());
                    if(!needRetry) {    // 不需要，则执行onComplete来收尾
                        context.onComplete();
                        return result;
                    }
                }else {
                    E exception = result.getLeft();
                    try {
                        context.onError(result.getLeft());
                    } catch (Exception e) {
                        return Either.left(exception);
                    }
                }
            } while(true);
        };
    }

    static <T> Supplier<Try<T>> decorateTrySupplier(Retry retry, Supplier<Try<T>> supplier) {
        return () -> {
            Retry.Context<T> context = retry.context();
            do {    // 先执行1次
                Try<T> result = supplier.get();
                if(result.isSuccess()) {
                    final boolean needRetry = context.onResult(result.get());
                    if(!needRetry) {    // 不需要，则执行onComplete来收尾
                        context.onComplete();
                        return result;
                    }
                }else {
                    Throwable cause = result.getCause();
                    if(cause instanceof Exception) {
                        try {
                            context.onError((Exception)result.getCause());
                        } catch (Exception e) {
                            return result;
                        }
                    }else {
                        return result;
                    }

                }
            } while(true);
        };
    }

    static <T> Callable<T> decorateCallable(Retry retry, Callable<T> callable) {
        return () -> {
            Retry.Context context = retry.context();
            do {    // 先执行1次
                try {
                    T result = callable.call();
                    final boolean needReply = context.onResult(result);
                    if(!needReply) {
                        context.onComplete();
                        return result;
                    }
                } catch (Exception e) {
                    context.onError(e);
                }
            } while(true);
        };
    }

    static Runnable decorateRunnable(Retry retry, Runnable runnable) {
        return () -> {
            Retry.Context context = retry.context();
            do {    // 先执行1次
                try {
                    runnable.run();
                    context.onComplete();
                    break;
                } catch (RuntimeException e) {
                    context.onRuntimeError(e);
                }
            } while(true);
        };
    }

    static <T, R> Function<T, R> decorateFunction(Retry retry, Function<T, R> function) {
        return (T t) -> {
            Retry.Context<R> context = retry.context();
            do {    // 先执行1次
                try {
                    R result = function.apply(t);
                    final boolean needReply = context.onResult(result);
                    if(!needReply) {
                        context.onComplete();
                        return result;
                    }
                } catch (RuntimeException e) {
                    context.onRuntimeError(e);
                }
            } while(true);
        };
    }

    String getName();

    <T> Retry.Context<T> context();

    <T> Retry.AsyncContext<T> asyncContext();

    RetryConfig getRetryConfig();

    Map<String, String> getTags();

    EventPublisher getEventPublisher();

    default <T> T executeCheckedSupplier(CheckedFunction0<T> checkedSupplier) throws Throwable {
        return decorateCheckedSupplier(this, checkedSupplier).apply();
    }

    default <T> T executeSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier).get();
    }

    default <E extends Exception, T> Either<E, T> executeEitherSupplier(Supplier<Either<E, T>> supplier) {
        return decorateEitherSupplier(this, supplier).get();
    }

    default <T> Try<T> executeTrySupplier(Supplier<Try<T>> supplier) {
        return decorateTrySupplier(this, supplier).get();
    }

    default <T> T executeCallable(Callable<T> callable) throws Exception {
        return decorateCallable(this, callable).call();
    }

    default void executeRunnable(Runnable runnable) {
        decorateRunnable(this, runnable).run();
    }

    default <T> CompletionStage<T> executeCompletionStage(ScheduledExecutorService scheduler, Supplier<CompletionStage<T>> supplier) {
        return decorateCompletionStage(this, scheduler, supplier).get();
    }

    Metrics getMetrics();

    interface Metrics {
        /**
         * 没有retry直接就成功的call次数
         **/
        long getNumberOfSuccessfulCallsWithoutRetryAttempt();

        /**
         * 没有retry直接就失败的call次数
         **/
        long getNumberOfFailedCallsWithoutRetryAttempt();

        /**
         * 经过retry之后成功的call次数
         **/
        long getNumberOfSuccessfulCallsWithRetryAttempt();

        /**
         * 经过retry之后失败的call次数
         **/
        long getNumberOfFailedCallsWithRetryAttempt();
    }

    /**
     * retry期间的上下文（异步版本）
     */
    interface AsyncContext<T> {

        /**
         * 具体实现会直接调用onComplete()方法
         */
        @Deprecated
        void onSuccess();

        /**
         * record一次成功或开始失败但重试成功的call，返回值是下一次retry的等待时间间隔
         */
        void onComplete();

        /**
         * record一次最终失败的call
         */
        long onError(Throwable t);

        /**
         * 检查call的返回值，重点是返回值类型是long，因此异步不能直接阻塞线程，是通过调用方的延迟线程调度器来实现的等待
         */
        long onResult(T result);
    }

    /**
     * retry期间的上下文（同步版本）
     */
    interface Context<T> {
        /**
         * 具体实现会直接调用onComplete()方法
         */
        @Deprecated
        void onSuccess();

        /**
         * 注意这个方法会在onResult之后，且结果为false才会被调用，到这里说明已经不需要retry了，并表明最终结果是成功
         */
        void onComplete();

        /**
         * 检查call的返回值，重点是返回值类型是boolean，为true说明要执行retry（retry之前要阻塞一定时间），
         * 注意这个方法是最早被执行的，类似于Spring Retry里面的canRetry(context)方法
         */
        boolean onResult(T result);


        /**
         * 处理受查的Exception（可以处理CheckFunction0这种能抛出受查异常的类型，用这个方法来接收处理）
         */
        void onError(Exception e) throws Exception;

        /**
         * 处理RuntimeException（在诸如Supplier这种不能抛出受查异常的类型，只能用这个方法来接收处理）
         */
        void onRuntimeError(RuntimeException e);
    }

    interface EventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<RetryEvent> {
        EventPublisher onRetry(EventConsumer<RetryOnRetryEvent> eventConsumer);
        EventPublisher onSuccess(EventConsumer<RetryOnSuccessEvent> eventConsumer);
        EventPublisher onError(EventConsumer<RetryOnErrorEvent> eventConsumer);
        EventPublisher onIgnoredError(EventConsumer<RetryOnIgnoredErrorEvent> eventConsumer);
    }

    class AsyncRetryBlock<T> implements Runnable {
        private final ScheduledExecutorService scheduler;
        private final Retry.AsyncContext<T> retryContext;
        private final Supplier<CompletionStage<T>> supplier;
        private final CompletableFuture<T> promise;

        AsyncRetryBlock(ScheduledExecutorService scheduler, Retry.AsyncContext<T> retryContext,
                        Supplier<CompletionStage<T>> supplier, CompletableFuture<T> promise) {
            this.scheduler = scheduler;
            this.retryContext = retryContext;
            this.supplier = supplier;
            this.promise = promise;
        }

        @Override
        public void run() {
            final CompletionStage<T> stage = this.supplier.get();
            stage.whenComplete((result, t) -> {
                if(t != null) {
                    if(t instanceof Exception)
                        this.onError((Exception)t);
                    else
                        this.promise.completeExceptionally(t);
                }else {
                    this.onResult(result);
                }
            });
        }

        /**
         * 处理受查Exception
         */
        private void onError(Exception e) {
            // 委托給RetryContext来处理，并返回retry等待时间
            final long delay = this.retryContext.onError(e);
            if(delay < 1)   // 立刻完成
                this.promise.completeExceptionally(e);
            else    // 延迟一段时间再运行一次自己的run方法（用专业术语讲，这个叫调度递归，不是传统的线性递归）
                this.scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
        }

        private void onResult(T result) {
            // 委托給RetryContext来处理，并返回retry等待时间
            final long delay = this.retryContext.onResult(result);
            if(delay < 1) { // 立刻完成
                try {
                    this.retryContext.onComplete();
                    this.promise.complete(result);
                } catch (Exception e) {
                    promise.completeExceptionally(e);
                }
            }else { // 延迟一段时间再运行一次自己的run方法（用专业术语讲，这个叫调度递归，不是传统的线性递归）
                this.scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
            }
        }
    }
}
