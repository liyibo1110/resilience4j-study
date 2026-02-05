package com.github.liyibo1110.resilience4j.core;

/**
 * 核心事件模型（消费者绑定器）
 * @author liyibo
 * @date 2026-02-04 13:43
 */
public interface EventPublisher<T> {

    void onEvent(EventConsumer<T> onEventConsumer);
}
