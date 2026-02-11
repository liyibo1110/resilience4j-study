package com.github.liyibo1110.resilience4j.reactor.ratelimiter.operator;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

/**
 * @author liyibo
 * @date 2026-02-10 23:04
 */
class FluxRateLimiter<T> extends FluxOperator<T, T> {
    private final CorePublisherRateLimiterOperator<T> operator;

    FluxRateLimiter(Flux<? extends T> source, RateLimiter rateLimiter) {
        super(source);
        this.operator = new CorePublisherRateLimiterOperator<T>(source, rateLimiter);
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        operator.subscribe(actual);
    }
}
