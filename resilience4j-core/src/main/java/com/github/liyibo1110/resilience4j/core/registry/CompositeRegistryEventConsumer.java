package com.github.liyibo1110.resilience4j.core.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RegistryEventConsumer的特殊实现，直接委托给RegistryEventConsumer集合来逐个消费
 * @author liyibo
 * @date 2026-02-04 15:03
 */
public class CompositeRegistryEventConsumer<E> implements RegistryEventConsumer {
    private final List<RegistryEventConsumer<E>> delegates;

    public CompositeRegistryEventConsumer(List<RegistryEventConsumer<E>> delegates) {
        Objects.requireNonNull(delegates);
        this.delegates = new ArrayList<>(delegates);
    }

    @Override
    public void onEntryAddedEvent(EntryAddedEvent event) {
        this.delegates.forEach(c -> c.onEntryAddedEvent(event));
    }

    @Override
    public void onEntryRemovedEvent(EntryRemovedEvent event) {
        this.delegates.forEach(c -> c.onEntryRemovedEvent(event));
    }

    @Override
    public void onEntryReplacedEvent(EntryReplacedEvent event) {
        this.delegates.forEach(c -> c.onEntryReplacedEvent(event));
    }
}
