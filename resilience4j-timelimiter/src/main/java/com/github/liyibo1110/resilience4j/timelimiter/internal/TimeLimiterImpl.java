package com.github.liyibo1110.resilience4j.timelimiter.internal;

import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterConfig;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnErrorEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnSuccessEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnTimeoutEvent;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-08 19:02
 */
public class TimeLimiterImpl implements TimeLimiter {
    private static final Logger LOG = LoggerFactory.getLogger(TimeLimiterImpl.class);

    private final String name;
    private final Map<String, String> tags;
    private final TimeLimiterConfig timeLimiterConfig;
    private final TimeLimiterEventProcessor eventProcessor;

    public TimeLimiterImpl(String name, TimeLimiterConfig timeLimiterConfig) {
        this(name, timeLimiterConfig, HashMap.empty());
    }

    public TimeLimiterImpl(String name, TimeLimiterConfig timeLimiterConfig,
                           io.vavr.collection.Map<String, String> tags) {
        this.name = name;
        this.tags = Objects.requireNonNull(tags, "Tags must not be null");
        this.timeLimiterConfig = timeLimiterConfig;
        this.eventProcessor = new TimeLimiterEventProcessor();
    }

    @Override
    public <T, F extends Future<T>> Callable<T> decorateFutureSupplier(Supplier<F> futureSupplier) {
        return () -> {
            Future<T> future = futureSupplier.get();    // 原始任务的异步结果
            try {
                T result = future.get(getTimeLimiterConfig().getTimeoutDuration().toMillis(), TimeUnit.MILLISECONDS);
                this.onSuccess();
                return result;
            } catch (TimeoutException e) {
                TimeoutException timeoutException = TimeLimiter.createdTimeoutExceptionWithName(name, e);
                this.onError(timeoutException);
                if(getTimeLimiterConfig().shouldCancelRunningFuture())
                    future.cancel(true);
                throw timeoutException;
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if(t == null) {
                    this.onError(e);
                    throw e;
                }
                this.onError(t);
                if(t instanceof Error)
                    throw (Error)t;
                throw (Exception)t;
            }
        };
    }

    @Override
    public <T, F extends CompletionStage<T>> Supplier<CompletionStage<T>> decorateCompletionStage(ScheduledExecutorService scheduler, Supplier<F> supplier) {
        return () -> {
            CompletableFuture<T> future = supplier.get().toCompletableFuture(); // 原始任务的异步结果
            // 用于检测超时的线程Future（内部已经开始延迟准备运作了，如果运行了，会把传入的future直接设置为超时异常）
            ScheduledFuture<?> timeoutFuture = Timeout.of(future, scheduler, name, getTimeLimiterConfig().getTimeoutDuration().toMillis(),
                    TimeUnit.MILLISECONDS);
            return future.whenComplete((result, t) -> {
                // future正常先完成，会进入下面的流程
                if(result != null) {
                    if(!timeoutFuture.isDone())
                        timeoutFuture.cancel(false);
                    this.onSuccess();
                }

                // timeoutFuture先完成会进入下面的流程
                if(t != null) {
                    if(t instanceof CompletionException) {  // 不是超时异常
                        Throwable cause = t.getCause();
                        this.onError(cause);
                    }else if(t instanceof ExecutionException) { // 也不是超时异常
                        Throwable cause = t.getCause();
                        if(cause == null)
                            this.onError(t);
                        else
                            this.onError(cause);
                    }else { // 可能是超时异常了
                        this.onError(t);
                    }
                }
            });
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public TimeLimiterConfig getTimeLimiterConfig() {
        return timeLimiterConfig;
    }

    @Override
    public EventPublisher getEventPublisher() {
        return eventProcessor;
    }

    @Override
    public void onSuccess() {
        if(!this.eventProcessor.hasConsumers())
            return;
        this.publishEvent(new TimeLimiterOnSuccessEvent(this.name));
    }

    @Override
    public void onError(Throwable t) {
        if(t instanceof TimeoutException)
            this.onTimeout();
        else
            this.onFailure(t);
    }

    private void onTimeout() {
        if(!this.eventProcessor.hasConsumers())
            return;
        this.publishEvent(new TimeLimiterOnTimeoutEvent(this.name));
    }

    private void onFailure(Throwable t) {
        if(!this.eventProcessor.hasConsumers())
            return;
        this.publishEvent(new TimeLimiterOnErrorEvent(this.name, t));
    }

    private void publishEvent(TimeLimiterEvent event) {
        try {
            this.eventProcessor.consumeEvent(event);
            LOG.debug("Event {} published: {}", event.getEventType(), event);
        } catch (Exception e) {
            LOG.warn("Failed to handle event {}", event.getEventType(), e);
        }
    }

    /**
     * 生成用来作为超时依据的延迟线程
     */
    static final class Timeout {
        private Timeout() {}

        /**
         * 生成延迟线程实例，里面只做一件事，就是把原始任务的CompletableFuture直接设定为超时异常
         */
        static ScheduledFuture<?> of(CompletableFuture<?> future, ScheduledExecutorService scheduler,
                                     String name, long delay, TimeUnit unit) {
            return scheduler.schedule(() -> {
                if(future != null && !future.isDone())
                    future.completeExceptionally(TimeLimiter.createdTimeoutExceptionWithName(name, null));
            }, delay, unit);
        }
    }
}
