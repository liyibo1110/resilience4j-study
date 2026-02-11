package com.github.liyibo1110.resilience4j.reactor.circuitbreaker.operator;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.reactor.IllegalPublisherException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

/**
 * Publisher -> Mono/Flux
 * @author liyibo
 * @date 2026-02-10 17:54
 */
public class CircuitBreakerOperator<T> implements UnaryOperator<Publisher<T>> {
    private final CircuitBreaker cb;

    private CircuitBreakerOperator(CircuitBreaker cb) {
        this.cb = cb;
    }

    public static <T> CircuitBreakerOperator<T> of(CircuitBreaker cb) {
        return new CircuitBreakerOperator<>(cb);
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if(publisher instanceof Mono)
            return new MonoCircuitBreaker<>((Mono<? extends T>)publisher, cb);
        else if(publisher instanceof Flux)
            return new FluxCircuitBreaker<>((Flux<? extends T>)publisher, cb);
        else
            throw new IllegalPublisherException(publisher);
    }
}
