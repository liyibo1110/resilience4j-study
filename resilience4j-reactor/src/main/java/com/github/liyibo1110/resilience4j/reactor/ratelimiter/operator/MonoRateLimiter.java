package com.github.liyibo1110.resilience4j.reactor.ratelimiter.operator;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;

/**
 * @author liyibo
 * @date 2026-02-10 23:02
 */
class MonoRateLimiter<T> extends MonoOperator<T, T> {
    private final CorePublisherRateLimiterOperator<T> operator;

    MonoRateLimiter(Mono<? extends T> source, RateLimiter rateLimiter) {
        super(source);
        this.operator = new CorePublisherRateLimiterOperator<T>(source, rateLimiter);
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        operator.subscribe(actual);
    }
}
