package com.github.liyibo1110.resilience4j.reactor.ratelimiter.operator;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.reactor.IllegalPublisherException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

/**
 * @author liyibo
 * @date 2026-02-10 23:05
 */
public class RateLimiterOperator<T> implements UnaryOperator<Publisher<T>> {
    private final RateLimiter rateLimiter;

    private RateLimiterOperator(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public static <T> RateLimiterOperator<T> of(RateLimiter rateLimiter) {
        return new RateLimiterOperator<>(rateLimiter);
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if(publisher instanceof Mono)
            return new MonoRateLimiter<>((Mono<? extends T>) publisher, rateLimiter);
        else if (publisher instanceof Flux)
            return new FluxRateLimiter<>((Flux<? extends T>) publisher, rateLimiter);
        else
            throw new IllegalPublisherException(publisher);
    }
}
