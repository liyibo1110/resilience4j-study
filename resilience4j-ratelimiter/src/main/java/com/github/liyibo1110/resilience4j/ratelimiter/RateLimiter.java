package com.github.liyibo1110.resilience4j.ratelimiter;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.exception.AcquirePermissionCancelledException;
import com.github.liyibo1110.resilience4j.core.metrics.Metrics;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnFailureEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnSuccessEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedRunnable;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-06 22:13
 */
public interface RateLimiter {

    static RateLimiter of(String name, RateLimiterConfig config) {
        return of(name, config, HashMap.empty());
    }

    static RateLimiter of(String name, RateLimiterConfig config, Map<String, String> tags) {
        return new AtomicRateLimiter(name, config, tags);
    }

    static RateLimiter of(String name, Supplier<RateLimiterConfig> configSupplier) {
        return of(name, configSupplier.get(), HashMap.empty());
    }

    static RateLimiter of(String name, Supplier<RateLimiterConfig> configSupplier,
                          Map<String, String> tags) {
        return new AtomicRateLimiter(name, configSupplier.get(), tags);
    }

    static RateLimiter ofDefaults(String name) {
        return new AtomicRateLimiter(name, RateLimiterConfig.ofDefaults());
    }

    static <T> Supplier<CompletionStage<T>> decorateCompletionStage(RateLimiter rateLimiter,
                                                                    Supplier<CompletionStage<T>> supplier) {
        return decorateCompletionStage(rateLimiter, 1, supplier);
    }

    static <T> Supplier<CompletionStage<T>> decorateCompletionStage(RateLimiter rateLimiter,
                                                                    int permits, Supplier<CompletionStage<T>> supplier) {
        return () -> {
            final CompletableFuture<T> promise = new CompletableFuture<>();
            try {
                waitForPermission(rateLimiter, permits);
                supplier.get().whenComplete((result, t) -> {
                    if(t == null) {
                        rateLimiter.onResult(result);
                        promise.complete(result);
                    }else {
                        rateLimiter.onError(t);
                        promise.completeExceptionally(t);
                    }
                });
            } catch (RequestNotPermitted requestNotPermitted) {
                promise.completeExceptionally(requestNotPermitted);
            } catch (Exception e) {
                rateLimiter.onError(e);
                promise.completeExceptionally(e);
            }
            return promise;
        };
    }

    static <T, F extends Future<T>> Supplier<F> decorateFuture(RateLimiter rateLimiter, Supplier<? extends F> supplier) {
        return decorateFuture(rateLimiter, 1, supplier);
    }

    static <T, F extends Future<T>> Supplier<F> decorateFuture(RateLimiter rateLimiter, int permits, Supplier<? extends F> supplier) {
        return () -> decorateSupplier(rateLimiter, permits, supplier).get();
    }

    static <T> CheckedFunction0<T> decorateCheckedSupplier(RateLimiter rateLimiter, CheckedFunction0<T> supplier) {
        return decorateCheckedSupplier(rateLimiter, 1, supplier);
    }

    static <T> CheckedFunction0<T> decorateCheckedSupplier(RateLimiter rateLimiter, int permits, CheckedFunction0<T> supplier) {
        return () -> {
            waitForPermission(rateLimiter, permits);
            try {
                T result = supplier.apply();
                rateLimiter.onResult(result);
                return result;
            } catch (Exception e) {
                rateLimiter.onError(e);
                throw e;
            }
        };
    }

    static CheckedRunnable decorateCheckedRunnable(RateLimiter rateLimiter, CheckedRunnable runnable) {
        return decorateCheckedRunnable(rateLimiter, 1, runnable);
    }

    static CheckedRunnable decorateCheckedRunnable(RateLimiter rateLimiter, int permits, CheckedRunnable runnable) {
        return () -> {
            waitForPermission(rateLimiter, permits);
            try {
                runnable.run();
                rateLimiter.onSuccess();
            } catch (Exception e) {
                rateLimiter.onError(e);
                throw e;
            }
        };
    }

    static <T, R> CheckedFunction1<T, R> decorateCheckedFunction(RateLimiter rateLimiter,
                                                                 CheckedFunction1<T, R> function) {
        return decorateCheckedFunction(rateLimiter, 1, function);
    }

    static <T, R> CheckedFunction1<T, R> decorateCheckedFunction(RateLimiter rateLimiter, int permits,
                                                                 CheckedFunction1<T, R> function) {
        return (T t) -> decorateCheckedSupplier(rateLimiter, permits, () -> function.apply(t)).apply();
    }

    static <T, R> CheckedFunction1<T, R> decorateCheckedFunction(RateLimiter rateLimiter, Function<T, Integer> permitsCalculator,
                                                                 CheckedFunction1<T, R> function) {
        return (T t) -> decorateCheckedFunction(rateLimiter, permitsCalculator.apply(t), function).apply(t);
    }

    static <T> Supplier<T> decorateSupplier(RateLimiter rateLimiter, Supplier<T> supplier) {
        return decorateSupplier(rateLimiter, 1, supplier);
    }

    static <T> Supplier<T> decorateSupplier(RateLimiter rateLimiter, int permits, Supplier<T> supplier) {
        return decorateCheckedSupplier(rateLimiter, permits, supplier::get).unchecked();
    }

    static <T> Supplier<Try<T>> decorateTrySupplier(RateLimiter rateLimiter, Supplier<Try<T>> supplier) {
        return decorateTrySupplier(rateLimiter, 1, supplier);
    }

    static <T> Supplier<Try<T>> decorateTrySupplier(RateLimiter rateLimiter, int permits, Supplier<Try<T>> supplier) {
        return () -> {
            try {
                waitForPermission(rateLimiter, permits);
                try {
                    Try<T> result = supplier.get();
                    if(result.isSuccess())
                        rateLimiter.onResult(result.get());
                    else
                        rateLimiter.onError(result.getCause());
                    return result;
                } catch (Exception e) {
                    rateLimiter.onError(e);
                    throw e;
                }
            } catch (RequestNotPermitted requestNotPermitted) {
                return Try.failure(requestNotPermitted);
            }
        };
    }

    static <T> Supplier<Either<Exception, T>> decorateEitherSupplier(RateLimiter rateLimiter, Supplier<Either<? extends Exception, T>> supplier) {
        return decorateEitherSupplier(rateLimiter, 1, supplier);
    }

    static <T> Supplier<Either<Exception, T>> decorateEitherSupplier(RateLimiter rateLimiter, int permits,
                                                                     Supplier<Either<? extends Exception, T>> supplier) {
        return () -> {
            try {
                waitForPermission(rateLimiter, permits);
                try {
                    Either<? extends Exception, T> result = supplier.get();
                    if(result.isRight())
                        rateLimiter.onResult(result.get());
                    else
                        rateLimiter.onError(result.getLeft());
                    return Either.narrow(result);
                } catch (Exception e) {
                    rateLimiter.onError(e);
                    throw e;
                }
            } catch (RequestNotPermitted requestNotPermitted) {
                return Either.left(requestNotPermitted);
            }
        };
    }

    static <T> Callable<T> decorateCallable(RateLimiter rateLimiter, Callable<T> callable) {
        return decorateCallable(rateLimiter, 1, callable);
    }

    static <T> Callable<T> decorateCallable(RateLimiter rateLimiter, int permits, Callable<T> callable) {
        return () -> decorateCheckedSupplier(rateLimiter, permits, callable::call).unchecked().get();
    }

    static <T> Consumer<T> decorateConsumer(RateLimiter rateLimiter, Consumer<T> consumer) {
        return decorateConsumer(rateLimiter, 1, consumer);
    }

    static <T> Consumer<T> decorateConsumer(RateLimiter rateLimiter, int permits, Consumer<T> consumer) {
        return (T t) -> {
            waitForPermission(rateLimiter, permits);
            try {
                consumer.accept(t);
                rateLimiter.onSuccess();
            } catch (Exception e) {
                rateLimiter.onError(e);
                throw e;
            }
        };
    }

    static <T> Consumer<T> decorateConsumer(RateLimiter rateLimiter, Function<T, Integer> permitsCalculator,
                                            Consumer<T> consumer) {
        return (T t) -> decorateConsumer(rateLimiter, permitsCalculator.apply(t), consumer).accept(t);
    }

    static Runnable decorateRunnable(RateLimiter rateLimiter, Runnable runnable) {
        return decorateRunnable(rateLimiter, 1, runnable);
    }

    static Runnable decorateRunnable(RateLimiter rateLimiter, int permits, Runnable runnable) {
        return decorateCheckedRunnable(rateLimiter, permits, runnable::run).unchecked();
    }

    static <T, R> Function<T, R> decorateFunction(RateLimiter rateLimiter, Function<T, R> function) {
        return decorateFunction(rateLimiter, 1, function);
    }

    static <T, R> Function<T, R> decorateFunction(RateLimiter rateLimiter, int permits, Function<T, R> function) {
        return decorateCheckedFunction(rateLimiter, permits, function::apply).unchecked();
    }

    static <T, R> Function<T, R> decorateFunction(RateLimiter rateLimiter, Function<T, Integer> permitsCalculator,
                                                  Function<T, R> function) {
        return (T t) -> decorateFunction(rateLimiter, permitsCalculator.apply(t), function).apply(t);
    }

    static void waitForPermission(final RateLimiter rateLimiter) {
        waitForPermission(rateLimiter, 1);
    }

    /**
     * 在默认的超时时间内等待达到所需数量的许可
     */
    static void waitForPermission(final RateLimiter rateLimiter, int permits) {
        boolean permission = rateLimiter.acquirePermission(permits);
        if(Thread.currentThread().isInterrupted())
            throw new AcquirePermissionCancelledException();
        if(!permission)
            throw RequestNotPermitted.createRequestNotPermitted(rateLimiter);
    }

    /**
     * 如果结果实例，通过了config中的drainPermissionsOnResult的检测，则运行drainPermissions
     */
    default void drainIfNeeded(Either<? extends Throwable, ?> callsResult) {
        Predicate<Either<? extends Throwable, ?>> checker = this.getRateLimiterConfig().getDrainPermissionsOnResult();
        if(checker != null && checker.test(callsResult))
            this.drainPermissions();
    }

    default <T> CompletionStage<T> executeCompletionStage(Supplier<CompletionStage<T>> supplier) {
        return decorateCompletionStage(this, supplier).get();
    }

    void changeTimeoutDuration(Duration timeoutDuration);

    void changeLimitForPeriod(int limitForPeriod);

    default boolean acquirePermission() {
        return acquirePermission(1);
    }

    /**
     * 请求n个许可，如没有可用的则会阻塞，直到获得许可，最大等待时间为timeoutDuration。
     * 如果在等待期间被中断，则不会抛出InterruptedException，但其中断状态将被设置。
     */
    boolean acquirePermission(int permits);

    default long reservePermission() {
        return this.reservePermission(1);
    }

    /**
     * 从rateLimiter中预定指定数量的许可，并返回等待该许可所需的纳秒数。
     * 如果返回负数，则表示未能预定许可，可能是因为timeoutDuration过小
     */
    long reservePermission(int permits);

    /**
     * 清空当前period的所有剩余的许可
     */
    void drainPermissions();

    /**
     * record一次失败的call，当一个call运行失败后，必须调用这个方法。
     */
    default void onError(Throwable t) {
        drainIfNeeded(Either.left(t));
    }

    /**
     * record一次成功的call，当一个call成功运行后，必须调用这个方法。
     */
    default void onSuccess() {
        drainIfNeeded(Either.right(null));
    }

    /**
     * call返回值后，必须调用这个方法，之后会尝试调用onSuccess或者onError（和retry差不多意思）
     */
    default void onResult(Object result) {
        drainIfNeeded(Either.right(result));
    }

    String getName();
    RateLimiterConfig getRateLimiterConfig();
    Map<String, String> getTags();
    Metrics getMetrics();
    EventPublisher getEventPublisher();

    default <T> T executeSupplier(Supplier<T> supplier) {
        return executeSupplier(1, supplier);
    }

    default <T> T executeSupplier(int permits, Supplier<T> supplier) {
        return decorateSupplier(this, permits, supplier).get();
    }

    default <T> Try<T> executeTrySupplier(Supplier<Try<T>> supplier) {
        return executeTrySupplier(1, supplier);
    }

    default <T> Try<T> executeTrySupplier(int permits, Supplier<Try<T>> supplier) {
        return decorateTrySupplier(this, permits, supplier).get();
    }

    default <T> Either<Exception, T> executeEitherSupplier(Supplier<Either<? extends Exception, T>> supplier) {
        return executeEitherSupplier(1, supplier);
    }

    default <T> Either<Exception, T> executeEitherSupplier(int permits, Supplier<Either<? extends Exception, T>> supplier) {
        return decorateEitherSupplier(this, permits, supplier).get();
    }

    default <T> T executeCallable(Callable<T> callable) throws Exception {
        return executeCallable(1, callable);
    }

    default <T> T executeCallable(int permits, Callable<T> callable) throws Exception {
        return decorateCallable(this, permits, callable).call();
    }

    default void executeRunnable(Runnable runnable) {
        executeRunnable(1, runnable);
    }

    default void executeRunnable(int permits, Runnable runnable) {
        decorateRunnable(this, permits, runnable).run();
    }

    default <T> T executeCheckedSupplier(CheckedFunction0<T> checkedSupplier) throws Throwable {
        return executeCheckedSupplier(1, checkedSupplier);
    }

    default <T> T executeCheckedSupplier(int permits, CheckedFunction0<T> checkedSupplier)
            throws Throwable {
        return decorateCheckedSupplier(this, permits, checkedSupplier).apply();
    }

    interface Metrics {
        int getNumberOfWaitingThreads();
        int getAvailablePermissions();
    }

    interface EventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<RateLimiterEvent> {
        EventPublisher onSuccess(EventConsumer<RateLimiterOnSuccessEvent> eventConsumer);

        EventPublisher onFailure(EventConsumer<RateLimiterOnFailureEvent> eventConsumer);
    }
}
