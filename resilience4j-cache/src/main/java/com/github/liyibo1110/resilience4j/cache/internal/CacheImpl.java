package com.github.liyibo1110.resilience4j.cache.internal;

import com.github.liyibo1110.resilience4j.cache.Cache;
import com.github.liyibo1110.resilience4j.cache.event.CacheEvent;
import com.github.liyibo1110.resilience4j.cache.event.CacheOnErrorEvent;
import com.github.liyibo1110.resilience4j.cache.event.CacheOnHitEvent;
import com.github.liyibo1110.resilience4j.cache.event.CacheOnMissEvent;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.EventProcessor;
import io.vavr.CheckedFunction0;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-09 14:29
 */
public class CacheImpl<K, V> implements Cache<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(CacheImpl.class);

    private final javax.cache.Cache<K, V> cache;
    private final CacheMetrics metrics;
    private final CacheEventProcessor eventProcessor;

    public CacheImpl(javax.cache.Cache<K, V> cache) {
        this.cache = cache;
        this.metrics = new CacheMetrics();
        this.eventProcessor = new CacheEventProcessor();
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public V computeIfAbsent(K cacheKey, CheckedFunction0<V> supplier) {
        return this.getValueFromCache(cacheKey).getOrElse(() -> computeAndPut(cacheKey, supplier));
    }

    private V computeAndPut(K cacheKey, CheckedFunction0<V> supplier) {
        return Try.of(supplier).andThen(value -> putValueIntoCache(cacheKey, value)).get();
    }

    private Option<V> getValueFromCache(K cacheKey) {
        try {
            Option<V> result = Option.of(this.cache.get(cacheKey));
            if(result.isDefined()) {
                this.onCacheHit(cacheKey);
                return result;
            }else {
                this.onCacheMiss(cacheKey);
                return result;
            }
        } catch (Exception e) {
            LOG.warn("Failed to get a value from Cache {}", getName(), e);
            this.onError(e);
            return Option.none();
        }
    }

    private void putValueIntoCache(K cacheKey, V value) {
        try {
            if(value != null)
                this.cache.put(cacheKey, value);
        } catch (Exception e) {
            LOG.warn("Failed to put a value into Cache {}", getName(), e);
            this.onError(e);
        }
    }

    private void onError(Throwable t) {
        this.publishCacheEvent(() -> new CacheOnErrorEvent(cache.getName(), t));
    }

    private void onCacheMiss(K cacheKey) {
        metrics.onCacheMiss();
        this.publishCacheEvent(() -> new CacheOnMissEvent<>(cache.getName(), cacheKey));
    }

    private void onCacheHit(K cacheKey) {
        metrics.onCacheHit();
        this.publishCacheEvent(() -> new CacheOnHitEvent<>(cache.getName(), cacheKey));
    }

    private void publishCacheEvent(Supplier<CacheEvent> event) {
        if(eventProcessor.hasConsumers())
            eventProcessor.processEvent(event.get());
    }

    @Override
    public EventPublisher getEventPublisher() {
        return eventProcessor;
    }

    private class CacheEventProcessor extends EventProcessor<CacheEvent> implements EventConsumer<CacheEvent>, EventPublisher {

        @Override
        public EventPublisher onCacheHit(EventConsumer<CacheOnHitEvent> eventConsumer) {
            registerConsumer(CacheOnHitEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onCacheMiss(EventConsumer<CacheOnMissEvent> eventConsumer) {
            registerConsumer(CacheOnMissEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onError(EventConsumer<CacheOnErrorEvent> eventConsumer) {
            registerConsumer(CacheOnErrorEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public void consumeEvent(CacheEvent event) {
            super.processEvent(event);
        }
    }

    private final class CacheMetrics implements Metrics {
        private final LongAdder cacheMisses;
        private final LongAdder cacheHits;

        private CacheMetrics() {
            cacheMisses = new LongAdder();
            cacheHits = new LongAdder();
        }

        void onCacheMiss() {
            cacheMisses.increment();
        }

        void onCacheHit() {
            cacheHits.increment();
        }

        @Override
        public long getNumberOfCacheHits() {
            return cacheHits.longValue();
        }

        @Override
        public long getNumberOfCacheMisses() {
            return cacheMisses.longValue();
        }
    }
}
