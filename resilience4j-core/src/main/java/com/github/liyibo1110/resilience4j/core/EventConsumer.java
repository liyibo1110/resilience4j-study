package com.github.liyibo1110.resilience4j.core;

/**
 * 核心事件模型（事件消费者）
 * @author liyibo
 * @date 2026-02-04 13:46
 */
@FunctionalInterface
public interface EventConsumer<T> {

    void consumeEvent(T event);
}
