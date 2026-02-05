package com.github.liyibo1110.resilience4j.core.registry;

/**
 * @author liyibo
 * @date 2026-02-04 14:51
 */
public interface RegistryEventConsumer<E> {

    void onEntryAddedEvent(EntryAddedEvent<E> event);

    void onEntryRemovedEvent(EntryRemovedEvent<E> event);

    void onEntryReplacedEvent(EntryReplacedEvent<E> event);
}
