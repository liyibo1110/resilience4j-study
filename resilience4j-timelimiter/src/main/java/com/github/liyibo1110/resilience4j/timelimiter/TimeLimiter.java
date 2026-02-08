package com.github.liyibo1110.resilience4j.timelimiter;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnErrorEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnSuccessEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnTimeoutEvent;
import com.github.liyibo1110.resilience4j.timelimiter.internal.TimeLimiterImpl;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * 这个模块比较简单，代码也很少，
 * 超时检测的核心实现方式就是同时和原始的异步任务一起启动一个Scheduler线程，来充当超时检测，
 * 如果原始的异步任务先完成，则尝试取消调检测线程，当检测线程开始执行了，则说明原始任务没有运行完，应标记为超时。
 * @author liyibo
 * @date 2026-02-08 18:43
 */
public interface TimeLimiter {
    String DEFAULT_NAME = "UNDEFINED";

    static TimeLimiter ofDefaults() {
        return new TimeLimiterImpl(DEFAULT_NAME, TimeLimiterConfig.ofDefaults());
    }

    static TimeLimiter ofDefaults(String name) {
        return new TimeLimiterImpl(name, TimeLimiterConfig.ofDefaults());
    }

    static TimeLimiter of(TimeLimiterConfig timeLimiterConfig) {
        return of(DEFAULT_NAME, timeLimiterConfig);
    }

    static TimeLimiter of(String name, TimeLimiterConfig timeLimiterConfig) {
        return new TimeLimiterImpl(name, timeLimiterConfig);
    }

    static TimeLimiter of(String name, TimeLimiterConfig timeLimiterConfig,
                          io.vavr.collection.Map<String, String> tags) {
        return new TimeLimiterImpl(name, timeLimiterConfig, tags);
    }

    static TimeLimiter of(Duration timeoutDuration) {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(timeoutDuration)
                .build();
        return new TimeLimiterImpl(DEFAULT_NAME, timeLimiterConfig);
    }

    static <T, F extends Future<T>> Callable<T> decorateFutureSupplier(TimeLimiter timeLimiter, Supplier<F> futureSupplier) {
        return timeLimiter.decorateFutureSupplier(futureSupplier);
    }

    static <T, F extends CompletionStage<T>> Supplier<CompletionStage<T>> decorateCompletionStage(
            TimeLimiter timeLimiter, ScheduledExecutorService scheduler, Supplier<F> supplier) {
        return timeLimiter.decorateCompletionStage(scheduler, supplier);
    }

    String getName();

    io.vavr.collection.Map<String, String> getTags();

    TimeLimiterConfig getTimeLimiterConfig();

    default <T, F extends Future<T>> T executeFutureSupplier(Supplier<F> futureSupplier) throws Exception {
        return decorateFutureSupplier(this, futureSupplier).call();
    }

    default <T, F extends CompletionStage<T>> CompletionStage<T> executeCompletionStage(
            ScheduledExecutorService scheduler, Supplier<F> supplier) {
        return decorateCompletionStage(this, scheduler, supplier).get();
    }

    <T, F extends Future<T>> Callable<T> decorateFutureSupplier(Supplier<F> futureSupplier);

    <T, F extends CompletionStage<T>> Supplier<CompletionStage<T>> decorateCompletionStage(
            ScheduledExecutorService scheduler, Supplier<F> supplier);

    EventPublisher getEventPublisher();

    void onSuccess();

    void onError(Throwable t);

    interface EventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<TimeLimiterEvent> {
        EventPublisher onSuccess(EventConsumer<TimeLimiterOnSuccessEvent> eventConsumer);

        EventPublisher onError(EventConsumer<TimeLimiterOnErrorEvent> eventConsumer);

        EventPublisher onTimeout(EventConsumer<TimeLimiterOnTimeoutEvent> eventConsumer);
    }

    static TimeoutException createdTimeoutExceptionWithName(String name, @Nullable Throwable t) {
        final TimeoutException e = new TimeoutException(String.format("TimeLimiter '%s' recorded a timeout exception.", name));
        if(e != null)
            e.setStackTrace(t.getStackTrace());
        return e;
    }
}
