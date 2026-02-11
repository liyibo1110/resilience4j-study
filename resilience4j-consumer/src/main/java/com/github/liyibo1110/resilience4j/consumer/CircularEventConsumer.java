package com.github.liyibo1110.resilience4j.consumer;

import com.github.liyibo1110.resilience4j.circularbuffer.CircularFifoBuffer;
import com.github.liyibo1110.resilience4j.circularbuffer.ConcurrentCircularFifoBuffer;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import io.vavr.collection.List;

/**
 * 基于CircularFifoBuffer实现的EventConsumer实现，消费行为就是加入到buffer里面。
 * 这个组件（甚至整个模块）的作用就是应对大量的组件消息生成，但只在队列中保存最后一部分数据，最终用来给spring actuator使用
 * @author liyibo
 * @date 2026-02-10 14:41
 */
public class CircularEventConsumer<T> implements EventConsumer<T> {

    private final CircularFifoBuffer<T> buffer;

    public CircularEventConsumer(int capacity) {
        this.buffer = new ConcurrentCircularFifoBuffer<>(capacity);
    }

    @Override
    public void consumeEvent(T event) {
        buffer.add(event);
    }

    public List<T> getBufferedEvents() {
        return buffer.toList();
    }
}
