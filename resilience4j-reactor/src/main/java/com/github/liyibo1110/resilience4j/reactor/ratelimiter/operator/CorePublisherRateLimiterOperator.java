package com.github.liyibo1110.resilience4j.reactor.ratelimiter.operator;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RequestNotPermitted;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.time.Duration;

/**
 * @author liyibo
 * @date 2026-02-10 23:02
 */
public class CorePublisherRateLimiterOperator<T> {
    private final CorePublisher<? extends T> source;
    private final RateLimiter rateLimiter;

    CorePublisherRateLimiterOperator(CorePublisher<? extends T> source, RateLimiter rateLimiter) {
        this.source = source;
        this.rateLimiter = rateLimiter;
    }

    void subscribe(CoreSubscriber<? super T> actual) {
        long waitDuration = rateLimiter.reservePermission();
        if(waitDuration >= 0) {
            if(waitDuration > 0)
                this.delaySubscription(actual, waitDuration);
            else
                source.subscribe(new RateLimiterSubscriber<>(rateLimiter, actual));
        }else {
            Operators.error(actual, RequestNotPermitted.createRequestNotPermitted(rateLimiter));
        }
    }

    private void delaySubscription(CoreSubscriber<? super T> actual, long waitDuration) {
        Mono.delay(Duration.ofNanos(waitDuration))
                .subscribe(delay -> source.subscribe(new RateLimiterSubscriber<>(rateLimiter, actual)));
    }
}
