package com.github.liyibo1110.resilience4j.core.metrics;

import com.github.liyibo1110.resilience4j.core.registry.EntryAddedEvent;
import com.github.liyibo1110.resilience4j.core.registry.EntryRemovedEvent;
import com.github.liyibo1110.resilience4j.core.registry.EntryReplacedEvent;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;

/**
 * registry event -> metrics发布，相当于桥接
 * @author liyibo
 * @date 2026-02-04 21:36
 */
public interface MetricsPublisher<E> extends RegistryEventConsumer<E> {
    void publishMetrics(E entry);
    void removeMetrics(E entry);

    /**
     * 收到added类型的event，将其转发出
     */
    @Override
    default void onEntryAddedEvent(EntryAddedEvent<E> event) {
        this.publishMetrics(event.getAddedEntry());
    }

    /**
     * 收到added类型的event，将其通知移除
     */
    @Override
    default void onEntryRemovedEvent(EntryRemovedEvent<E> event) {
        this.removeMetrics(event.getRemovedEntry());
    }

    /**
     * 收到replaced类型的event，先移除旧entry，再发布新entry
     */
    @Override
    default void onEntryReplacedEvent(EntryReplacedEvent<E> event) {
        this.removeMetrics(event.getOldEntry());
        this.publishMetrics(event.getNewEntry());
    }
}
