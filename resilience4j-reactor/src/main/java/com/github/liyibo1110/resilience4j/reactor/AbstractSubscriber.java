package com.github.liyibo1110.resilience4j.reactor;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.util.context.Context;

/**
 * @author liyibo
 * @date 2026-02-10 17:45
 */
public abstract class AbstractSubscriber<T> extends BaseSubscriber<T> {
    /** 真正的下游消费者 */
    protected final CoreSubscriber<? super T> downstreamSubscriber;

    protected AbstractSubscriber(CoreSubscriber<? super T> downstreamSubscriber) {
        this.downstreamSubscriber = downstreamSubscriber;
    }

    /**
     * 调用downstreamSubscriber的onSubscribe方法
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        downstreamSubscriber.onSubscribe(this);
    }

    @Override
    public Context currentContext() {
        return downstreamSubscriber.currentContext();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
