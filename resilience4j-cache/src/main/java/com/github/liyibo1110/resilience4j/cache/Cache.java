package com.github.liyibo1110.resilience4j.cache;

import com.github.liyibo1110.resilience4j.cache.event.CacheEvent;
import com.github.liyibo1110.resilience4j.cache.event.CacheOnErrorEvent;
import com.github.liyibo1110.resilience4j.cache.event.CacheOnHitEvent;
import com.github.liyibo1110.resilience4j.cache.event.CacheOnMissEvent;
import com.github.liyibo1110.resilience4j.cache.internal.CacheImpl;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-09 14:27
 */
public interface Cache<K, V> {

    static <K, V> Cache<K, V> of(javax.cache.Cache<K, V> cache) {
        Objects.requireNonNull(cache, "Cache must not be null");
        return new CacheImpl<>(cache);
    }

    static <K, R> CheckedFunction1<K, R> decorateCheckedSupplier(Cache<K, R> cache, CheckedFunction0<R> supplier) {
        return (K cacheKey) -> cache.computeIfAbsent(cacheKey, supplier);
    }

    static <K, R> Function<K, R> decorateSupplier(Cache<K, R> cache, Supplier<R> supplier) {
        return (K cacheKey) -> cache.computeIfAbsent(cacheKey, supplier::get);
    }

    static <K, R> CheckedFunction1<K, R> decorateCallable(Cache<K, R> cache, Callable<R> callable) {
        return (K cacheKey) -> cache.computeIfAbsent(cacheKey, callable::call);
    }

    String getName();

    Metrics getMetrics();

    V computeIfAbsent(K key, CheckedFunction0<V> supplier);

    EventPublisher getEventPublisher();

    interface Metrics {
        long getNumberOfCacheHits();
        long getNumberOfCacheMisses();
    }

    interface EventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<CacheEvent> {
        EventPublisher onCacheHit(EventConsumer<CacheOnHitEvent> eventConsumer);
        EventPublisher onCacheMiss(EventConsumer<CacheOnMissEvent> eventConsumer);
        EventPublisher onError(EventConsumer<CacheOnErrorEvent> eventConsumer);
    }
}
