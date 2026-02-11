package com.github.liyibo1110.resilience4j.reactor.bulkhead.operator;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.reactor.IllegalPublisherException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

/**
 * @author liyibo
 * @date 2026-02-10 22:59
 */
public class BulkheadOperator<T> implements UnaryOperator<Publisher<T>> {
    private final Bulkhead bulkhead;

    private BulkheadOperator(Bulkhead bulkhead) {
        this.bulkhead = bulkhead;
    }

    public static <T> BulkheadOperator<T> of(Bulkhead bulkhead) {
        return new BulkheadOperator<>(bulkhead);
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if(publisher instanceof Mono)
            return new MonoBulkhead<>((Mono<? extends T>) publisher, bulkhead);
        else if (publisher instanceof Flux)
            return new FluxBulkhead<>((Flux<? extends T>) publisher, bulkhead);
        else
            throw new IllegalPublisherException(publisher);
    }
}
