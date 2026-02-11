package com.github.liyibo1110.resilience4j.reactor.circuitbreaker.operator;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.reactor.AbstractSubscriber;
import reactor.core.CoreSubscriber;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

/**
 * 这个是中间的Subscriber，负责把CircuitBreaker实例的onSuccess/onError/onResult，写入到Flux/Mono中去
 * @author liyibo
 * @date 2026-02-10 18:14
 */
class CircuitBreakerSubscriber<T> extends AbstractSubscriber<T> {
    private final CircuitBreaker cb;

    /** 用于计算耗时 */
    private final long start;

    /** 为true代表Mono（单个值），为false代表Flux（代表可能多个值） */
    private final boolean singleProducer;

    /** 保证只触发一次success/onResult，因为Reactive可能多次调用onNext，最后再调用onComplete，但cb只能统计一次调用 */
    private final AtomicBoolean successSignaled = new AtomicBoolean(false);

    /** 是否至少收到过一个onNext，用于区分空流和有值流 */
    private final AtomicBoolean eventWasEmitted = new AtomicBoolean(false);

    protected CircuitBreakerSubscriber(CircuitBreaker cb,
                                       CoreSubscriber<? super T> downstreamSubscriber,
                                       boolean singleProducer) {
        super(downstreamSubscriber);
        this.cb = requireNonNull(cb);
        this.singleProducer = singleProducer;
        this.start = cb.getCurrentTimestamp();
    }

    /**
     * onNext里面会调用这个方法
     */
    @Override
    protected void hookOnNext(T value) {
        if(!isDisposed()) { // 没有被cancel
            // 单值（构建这个实例时，传来的构造参数）才允许调用cb的onResult，多个值没法统计
            if(singleProducer && successSignaled.compareAndSet(false, true))
                cb.onResult(cb.getCurrentTimestamp() - start, cb.getTimestampUnit(), value);
            eventWasEmitted.set(true);  // 标记有事件产生
            // 直接调用下游
            downstreamSubscriber.onNext(value);
        }
    }

    /**
     * 正常完成
     */
    @Override
    protected void hookOnComplete() {
        // 如果还没有被标记成功，则标记并且调用cb的onSuccess
        if(successSignaled.compareAndSet(false, true))
            cb.onSuccess(cb.getCurrentTimestamp() - start, cb.getTimestampUnit());
        downstreamSubscriber.onComplete();
    }

    /**
     * 这个会在下游主动取消订阅时才会被调用
     */
    @Override
    public void hookOnCancel() {
        if(!successSignaled.get()) {
            if(eventWasEmitted.get())
                cb.onSuccess(cb.getCurrentTimestamp() - start, cb.getTimestampUnit());
            else
                cb.releasePermission();
        }
    }

    @Override
    protected void hookOnError(Throwable e) {
        cb.onError(cb.getCurrentTimestamp() - start, cb.getTimestampUnit(), e);
        downstreamSubscriber.onError(e);
    }
}
