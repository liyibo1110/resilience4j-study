package com.github.liyibo1110.resilience4j.retry.internal;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.EventProcessor;
import com.github.liyibo1110.resilience4j.core.IntervalBiFunction;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.retry.MaxRetriesExceeded;
import com.github.liyibo1110.resilience4j.retry.MaxRetriesExceededException;
import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryConfig;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnErrorEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnIgnoredErrorEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnRetryEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnSuccessEvent;
import io.vavr.CheckedConsumer;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Retry接口的实现
 * @author liyibo
 * @date 2026-02-06 11:54
 */
public class RetryImpl<T> implements Retry {

    static CheckedConsumer<Long> sleepFunction = Thread::sleep;
    private final Metrics metrics;
    private final RetryEventProcessor eventProcessor;

    /** 重要组件，判定一次call的结果，是否要retry */
    @Nullable
    private final Predicate<T> resultPredicate;
    private final String name;
    private final RetryConfig config;
    private final Map<String, String> tags;


    private final int maxAttempts;

    /** 超出retry最大次数是否抛异常 */
    private final boolean failAfterMaxAttempts;

    private final IntervalBiFunction<T> intervalBiFunction;
    private final Predicate<Throwable> exceptionPredicate;
    private final LongAdder succeededAfterRetryCounter;
    private final LongAdder failedAfterRetryCounter;
    private final LongAdder succeededWithoutRetryCounter;
    private final LongAdder failedWithoutRetryCounter;

    public RetryImpl(String name, RetryConfig config) {
        this(name, config, HashMap.empty());
    }

    public RetryImpl(String name, RetryConfig config, Map<String, String> tags) {
        this.name = name;
        this.config = config;
        this.tags = tags;
        this.maxAttempts = config.getMaxAttempts();
        this.failAfterMaxAttempts = config.isFailAfterMaxAttempts();
        this.intervalBiFunction = config.getIntervalBiFunction();
        this.exceptionPredicate = config.getExceptionPredicate();
        this.resultPredicate = config.getResultPredicate();
        this.metrics = this.new RetryMetrics();
        this.eventProcessor = new RetryEventProcessor();
        this.succeededAfterRetryCounter = new LongAdder();
        this.failedAfterRetryCounter = new LongAdder();
        this.succeededWithoutRetryCounter = new LongAdder();
        this.failedWithoutRetryCounter = new LongAdder();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Context context() {
        return new ContextImpl();
    }

    @Override
    public AsyncContext asyncContext() {
        return new AsyncContextImpl();
    }

    @Override
    public RetryConfig getRetryConfig() {
        return config;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    private void publishRetryEvent(Supplier<RetryEvent> event) {
        if(this.eventProcessor.hasConsumers())
            this.eventProcessor.consumeEvent(event.get());
    }

    @Override
    public EventPublisher getEventPublisher() {
        return eventProcessor;
    }

    @Override
    public Metrics getMetrics() {
        return this.metrics;
    }

    /**
     * 重要组件，RetryContext上下文的实现
     */
    public final class ContextImpl implements Retry.Context<T> {
        /** 已重试的次数 */
        private final AtomicInteger numOfAttempts = new AtomicInteger(0);

        /** 上一次失败的对应受查异常 */
        private final AtomicReference<Exception> lastException = new AtomicReference<>();

        /** 上一次失败的对应Runtime异常 */
        private final AtomicReference<RuntimeException> lastRuntimeException = new AtomicReference<>();

        private ContextImpl() {}

        @Override
        @Deprecated
        public void onSuccess() {
            this.onComplete();
        }

        @Override
        public void onComplete() {
            int currentNumOfAttempts = numOfAttempts.get();
            if(currentNumOfAttempts > 0 && currentNumOfAttempts < maxAttempts) {    // retry后成功，且不超过最大retry次数
                succeededAfterRetryCounter.increment(); // 对应计数器增加值
                Throwable t = Option.of(lastException.get()).getOrElse(lastRuntimeException.get()); // 检测这次retry是否有异常
                publishRetryEvent(() -> new RetryOnSuccessEvent(getName(), currentNumOfAttempts, t));
            }else { // 2种可能，超出最大重试次数了，或者第一次执行就成功了，没有retry
                if(currentNumOfAttempts >= maxAttempts) {
                    failedAfterRetryCounter.increment();
                    Throwable t = Option.of(lastException.get())
                            .orElse(Option.of(lastRuntimeException.get()))
                            .filter(e -> !failAfterMaxAttempts) // 如果failAfterMaxAttempts为true才会执行最后的抛MaxRetriesExceeded异常
                            .getOrElse(new MaxRetriesExceeded("max retries is reached out for the result predicate check"));
                    publishRetryEvent(() -> new RetryOnErrorEvent(getName(), currentNumOfAttempts, t)); // t可能是null，看failAfterMaxAttempts的值

                    if(failAfterMaxAttempts)    // failAfterMaxAttempts为true，最终结果就是抛MaxRetriesExceededException异常
                        throw MaxRetriesExceededException.createMaxRetriesExceededException(RetryImpl.this);
                }else { // 无retry直接成功
                    succeededWithoutRetryCounter.increment();
                }
            }
        }

        @Override
        public boolean onResult(T result) {
            if(resultPredicate != null && resultPredicate.test(result)) {
                // 增加retry次数计数
                int currentNumOfAttempts = numOfAttempts.incrementAndGet();
                if(currentNumOfAttempts >= maxAttempts) {
                    return false;   // 进入onComplete再进一步判断了
                }else {
                    this.waitIntervalAfterFailure(currentNumOfAttempts, Either.right(result));  // 阻塞等待
                    return true;
                }
            }
            return false;   // 运行成功了
        }

        @Override
        public void onError(Exception e) throws Exception {
            if(exceptionPredicate.test(e)) {    // 命中了这次的异常
                lastException.set(e);
                this.throwOrSleepAfterException();
            }else { // 异常不在监控范围内，直接抛出，不再retry
                failedWithoutRetryCounter.increment();
                publishRetryEvent(() -> new RetryOnIgnoredErrorEvent(getName(), e));
                throw e;
            }
        }

        @Override
        public void onRuntimeError(RuntimeException e) {
            if(exceptionPredicate.test(e)) {
                lastRuntimeException.set(e);
                this.throwOrSleepAfterRuntimeException();
            }else { // 异常不在监控范围内，直接抛出，不再retry
                failedWithoutRetryCounter.increment();
                publishRetryEvent(() -> new RetryOnIgnoredErrorEvent(getName(), e));
                throw e;
            }
        }

        private void throwOrSleepAfterException() throws Exception {
            int currentNumOfAttempts = numOfAttempts.incrementAndGet(); // error是在这里自增的retry次数
            Exception t = lastException.get();
            if(currentNumOfAttempts >= maxAttempts) {   // 超过了直接算失败
                failedAfterRetryCounter.increment();
                publishRetryEvent(() -> new RetryOnErrorEvent(getName(), currentNumOfAttempts, t));
                throw t;
            }else { // 不超过就在这里等待
                waitIntervalAfterFailure(currentNumOfAttempts, Either.left(t));
            }
        }

        private void throwOrSleepAfterRuntimeException() {
            int currentNumOfAttempts = numOfAttempts.incrementAndGet(); // error是在这里自增的retry次数
            RuntimeException t = lastRuntimeException.get();
            if(currentNumOfAttempts >= maxAttempts) {   // 超过了直接算失败
                failedAfterRetryCounter.increment();
                publishRetryEvent(() -> new RetryOnErrorEvent(getName(), currentNumOfAttempts, t));
                throw t;
            }else { // 不超过就在这里等待
                waitIntervalAfterFailure(currentNumOfAttempts, Either.left(t));
            }
        }

        private void waitIntervalAfterFailure(int currentNumOfAttempts, Either<Throwable, T> either) {
            long interval = intervalBiFunction.apply(numOfAttempts.get(), either);
            publishRetryEvent(() -> new RetryOnRetryEvent(getName(), currentNumOfAttempts, either.swap().getOrNull(), interval));
            Try.run(() -> sleepFunction.accept(interval)).getOrElseThrow(ex -> lastRuntimeException.get());
        }
    }

    /**
     * 重要组件，AsyncRetryContext上下文的实现
     */
    public final class AsyncContextImpl implements Retry.AsyncContext<T> {
        private final AtomicInteger numOfAttempts = new AtomicInteger(0);
        private final AtomicReference<Throwable> lastException = new AtomicReference<>();

        @Override
        public void onSuccess() {
            this.onComplete();
        }

        @Override
        public void onComplete() {
            int currentNumOfAttempts = numOfAttempts.get();
            if(currentNumOfAttempts > 0 && currentNumOfAttempts < maxAttempts) {
                succeededAfterRetryCounter.increment();
                publishRetryEvent(() -> new RetryOnSuccessEvent(name, currentNumOfAttempts, lastException.get()));
            }else {
                if(currentNumOfAttempts >= maxAttempts) {
                    failedAfterRetryCounter.increment();
                    Throwable t = Option.of(lastException.get())
                            .filter(e -> !failAfterMaxAttempts)
                            .getOrElse(new MaxRetriesExceeded("max retries is reached out for the result predicate check"));
                    publishRetryEvent(() -> new RetryOnErrorEvent(name, currentNumOfAttempts, t));
                    if(failAfterMaxAttempts)
                        throw MaxRetriesExceededException.createMaxRetriesExceededException(RetryImpl.this);
                }else {
                    succeededWithoutRetryCounter.increment();
                }
            }
        }

        @Override
        public long onError(Throwable t) {
            if(t instanceof CompletionException || t instanceof ExecutionException) {
                Throwable cause = t.getCause();
                return handleThrowable(cause);
            }else {
                return handleThrowable(t);
            }
        }

        private long handleThrowable(Throwable t) {
            if(!exceptionPredicate.test(t)) {   // 异常不在监控范围内，直接抛出，不再retry
                failedWithoutRetryCounter.increment();
                publishRetryEvent(() -> new RetryOnIgnoredErrorEvent(getName(), t));
                return -1;
            }
            return this.handleOnError(t);   // 在监控范围内，继续判断
        }

        private long handleOnError(Throwable t) {
            lastException.set(t);
            int attempt = numOfAttempts.incrementAndGet();
            if(attempt >= maxAttempts) {
                failedAfterRetryCounter.increment();
                publishRetryEvent(() -> new RetryOnErrorEvent(name, attempt, t));
                return -1;
            }
            // 可以retry
            long interval = intervalBiFunction.apply(attempt, Either.left(t));
            publishRetryEvent(() -> new RetryOnRetryEvent(getName(), attempt, t, interval));
            return interval;
        }

        @Override
        public long onResult(T result) {
            if(resultPredicate != null && resultPredicate.test(result)) {
                int attempt = numOfAttempts.incrementAndGet();
                if(attempt >= maxAttempts)
                    return -1;
                // 注意异步模式，并不会在这里直接阻塞，而是返回计算并返回等待时间
                return intervalBiFunction.apply(attempt, Either.right(result)); // 计算并返回这一次的等待时间
            }else {
                return -1;  // 执行成功，不需要retry了
            }
        }
    }

    public final class RetryMetrics implements Metrics {

        private RetryMetrics() {}

        @Override
        public long getNumberOfSuccessfulCallsWithoutRetryAttempt() {
            return succeededWithoutRetryCounter.longValue();
        }

        @Override
        public long getNumberOfFailedCallsWithoutRetryAttempt() {
            return failedWithoutRetryCounter.longValue();
        }

        @Override
        public long getNumberOfSuccessfulCallsWithRetryAttempt() {
            return succeededAfterRetryCounter.longValue();
        }

        @Override
        public long getNumberOfFailedCallsWithRetryAttempt() {
            return failedAfterRetryCounter.longValue();
        }
    }

    private class RetryEventProcessor extends EventProcessor<RetryEvent> implements EventConsumer<RetryEvent>, EventPublisher {

        @Override
        public void consumeEvent(RetryEvent event) {
            super.processEvent(event);
        }

        @Override
        public EventPublisher onRetry(EventConsumer<RetryOnRetryEvent> eventConsumer) {
            registerConsumer(RetryOnRetryEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onSuccess(EventConsumer<RetryOnSuccessEvent> eventConsumer) {
            registerConsumer(RetryOnSuccessEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onError(EventConsumer<RetryOnErrorEvent> eventConsumer) {
            registerConsumer(RetryOnErrorEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onIgnoredError(EventConsumer<RetryOnIgnoredErrorEvent> eventConsumer) {
            registerConsumer(RetryOnIgnoredErrorEvent.class.getName(), eventConsumer);
            return this;
        }
    }
}
