package com.github.liyibo1110.resilience4j.consumer;

import io.vavr.collection.Seq;

/**
 * 用来生成特定的CircularEventConsumer实例
 * @author liyibo
 * @date 2026-02-10 14:50
 */
public interface EventConsumerRegistry<T> {

    CircularEventConsumer<T> createEventConsumer(String id, int bufferSize);

    CircularEventConsumer<T> getEventConsumer(String id);

    Seq<CircularEventConsumer<T>> getAllEventConsumer();
}
