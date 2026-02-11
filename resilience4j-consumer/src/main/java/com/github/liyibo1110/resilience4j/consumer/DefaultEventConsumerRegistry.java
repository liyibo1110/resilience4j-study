package com.github.liyibo1110.resilience4j.consumer;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * EventConsumerRegistry的默认实现（基于ConcurrentHashMap）
 * @author liyibo
 * @date 2026-02-10 14:52
 */
public class DefaultEventConsumerRegistry<T> implements EventConsumerRegistry<T> {
    private final ConcurrentMap<String, CircularEventConsumer<T>> registry;

    public DefaultEventConsumerRegistry() {
        this.registry = new ConcurrentHashMap<>();
    }

    @Override
    public CircularEventConsumer<T> createEventConsumer(String id, int bufferSize) {
        CircularEventConsumer<T> eventConsumer = new CircularEventConsumer<>(bufferSize);
        registry.put(id, eventConsumer);
        return eventConsumer;
    }

    @Override
    public CircularEventConsumer<T> getEventConsumer(String id) {
        return registry.get(id);
    }

    @Override
    public Seq<CircularEventConsumer<T>> getAllEventConsumer() {
        return Array.ofAll(registry.values());
    }
}
