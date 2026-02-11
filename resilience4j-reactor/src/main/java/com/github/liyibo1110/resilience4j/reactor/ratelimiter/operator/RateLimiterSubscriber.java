package com.github.liyibo1110.resilience4j.reactor.ratelimiter.operator;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.reactor.AbstractSubscriber;
import reactor.core.CoreSubscriber;

/**
 * @author liyibo
 * @date 2026-02-10 23:01
 */
class RateLimiterSubscriber<T> extends AbstractSubscriber<T> {
    private final RateLimiter rateLimiter;

    protected RateLimiterSubscriber(RateLimiter rateLimiter, CoreSubscriber<? super T> downstreamSubscriber) {
        super(downstreamSubscriber);
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void hookOnNext(T value) {
        if(!isDisposed()) {
            rateLimiter.onResult(value);
            downstreamSubscriber.onNext(value);
        }
    }

    @Override
    public void hookOnError(Throwable t) {
        rateLimiter.onError(t);
        downstreamSubscriber.onError(t);
    }

    @Override
    public void hookOnComplete() {
        downstreamSubscriber.onComplete();
    }
}
