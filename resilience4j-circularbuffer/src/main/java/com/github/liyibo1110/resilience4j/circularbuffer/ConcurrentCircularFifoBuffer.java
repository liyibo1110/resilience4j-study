package com.github.liyibo1110.resilience4j.circularbuffer;

import io.vavr.collection.List;
import io.vavr.control.Option;

import java.util.Arrays;

/**
 * @author liyibo
 * @date 2026-02-10 14:37
 */
public class ConcurrentCircularFifoBuffer<T> implements CircularFifoBuffer<T> {
    private final ConcurrentEvictingQueue<T> queue;
    private final int capacity;

    public ConcurrentCircularFifoBuffer(int capacity) {
        queue = new ConcurrentEvictingQueue<>(capacity);
        this.capacity = capacity;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean isFull() {
        return queue.size() == capacity;
    }

    @Override
    public io.vavr.collection.List<T> toList() {
        T[] array = (T[]) queue.toArray();
        return List.ofAll(Arrays.asList(array));
    }

    @Override
    public void add(T element) {
        queue.offer(element);
    }

    @Override
    public Option<T> take() {
        return Option.of(queue.poll());
    }
}
