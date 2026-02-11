package com.github.liyibo1110.resilience4j.reactor.circuitbreaker.operator;

import com.github.liyibo1110.resilience4j.circuitbreaker.CallNotPermittedException;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;
import reactor.core.publisher.Operators;

/**
 * @author liyibo
 * @date 2026-02-10 18:13
 */
public class MonoCircuitBreaker<T> extends MonoOperator<T, T> {
    private CircuitBreaker cb;

    MonoCircuitBreaker(Mono<? extends T> source, CircuitBreaker cb) {
        super(source);
        this.cb = cb;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        if(cb.tryAcquirePermission())
            source.subscribe(new CircuitBreakerSubscriber(cb, actual, true));
        else
            Operators.error(actual, CallNotPermittedException.createCallNotPermittedException(cb));
    }
}
