package com.github.liyibo1110.resilience4j.reactor.circuitbreaker.operator;

import com.github.liyibo1110.resilience4j.circuitbreaker.CallNotPermittedException;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Operators;

/**
 * @author liyibo
 * @date 2026-02-10 22:50
 */
class FluxCircuitBreaker<T> extends FluxOperator<T, T> {
    private CircuitBreaker cb;

    FluxCircuitBreaker(Flux<? extends T> source, CircuitBreaker cb) {
        super(source);
        this.cb = cb;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        if(cb.tryAcquirePermission())
            source.subscribe(new CircuitBreakerSubscriber<>(cb, actual, false));
        else
            Operators.error(actual, CallNotPermittedException.createCallNotPermittedException(cb));
    }
}
