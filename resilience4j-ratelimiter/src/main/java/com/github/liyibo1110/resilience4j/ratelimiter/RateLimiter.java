package com.github.liyibo1110.resilience4j.ratelimiter;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.exception.AcquirePermissionCancelledException;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnFailureEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnSuccessEvent;
import io.vavr.collection.Map;
import io.vavr.control.Either;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-06 22:13
 */
public interface RateLimiter {

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
     * 排空当前period的所有剩余的许可
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

    interface Metrics {
        int getNumberOfWaitingThreads();
        int getAvailablePermissions();
    }

    interface EventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<RateLimiterEvent> {
        EventPublisher onSuccess(EventConsumer<RateLimiterOnSuccessEvent> eventConsumer);

        EventPublisher onFailure(EventConsumer<RateLimiterOnFailureEvent> eventConsumer);
    }
}
