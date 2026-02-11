package com.github.liyibo1110.resilience4j.circularbuffer;

import io.vavr.collection.List;
import io.vavr.control.Option;

/**
 * 高性能固定容量的环形缓冲区（先进先出）
 * @author liyibo
 * @date 2026-02-10 11:18
 */
public interface CircularFifoBuffer<T> {

    int size();

    boolean isEmpty();

    boolean isFull();

    List<T> toList();

    void add(T element);

    Option<T> take();
}
