package com.github.liyibo1110.resilience4j.reactor.bulkhead.operator;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.reactor.AbstractSubscriber;
import reactor.core.CoreSubscriber;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liyibo
 * @date 2026-02-10 22:56
 */
class BulkheadSubscriber<T> extends AbstractSubscriber<T> {
    private final Bulkhead bulkhead;
    private final boolean singleProducer;

    private final AtomicBoolean eventWasEmitted = new AtomicBoolean(false);
    private final AtomicBoolean successSignaled = new AtomicBoolean(false);

    BulkheadSubscriber(Bulkhead bulkhead, CoreSubscriber<? super T> downstreamSubscriber, boolean singleProducer) {
        super(downstreamSubscriber);
        this.bulkhead = Objects.requireNonNull(bulkhead);
        this.singleProducer = singleProducer;
    }

    @Override
    public void hookOnNext(T t) {
        if(!isDisposed()) {
            if(singleProducer && successSignaled.compareAndSet(false, true))
                bulkhead.onComplete();
            eventWasEmitted.set(true);
            downstreamSubscriber.onNext(t);
        }
    }

    @Override
    public void hookOnCancel() {
        if(!successSignaled.get()) {
            if(eventWasEmitted.get())
                bulkhead.onComplete();
            else
                bulkhead.releasePermission();
        }
    }

    @Override
    public void hookOnError(Throwable t) {
        bulkhead.onComplete();
        downstreamSubscriber.onError(t);
    }

    @Override
    public void hookOnComplete() {
        if(successSignaled.compareAndSet(false, true))
            bulkhead.onComplete();
        downstreamSubscriber.onComplete();
    }
}
