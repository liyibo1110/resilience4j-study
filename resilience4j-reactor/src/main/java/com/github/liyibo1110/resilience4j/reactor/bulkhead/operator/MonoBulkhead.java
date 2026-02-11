package com.github.liyibo1110.resilience4j.reactor.bulkhead.operator;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadFullException;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;
import reactor.core.publisher.Operators;

/**
 * @author liyibo
 * @date 2026-02-10 22:59
 */
class MonoBulkhead<T> extends MonoOperator<T, T> {
    private final Bulkhead bulkhead;

    MonoBulkhead(Mono<? extends T> source, Bulkhead bulkhead) {
        super(source);
        this.bulkhead = bulkhead;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        if(bulkhead.tryAcquirePermission())
            source.subscribe(new BulkheadSubscriber<>(bulkhead, actual, true));
        else
            Operators.error(actual, BulkheadFullException.createBulkheadFullException(bulkhead));
    }
}
