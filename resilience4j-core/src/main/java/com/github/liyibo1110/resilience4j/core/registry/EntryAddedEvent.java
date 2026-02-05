package com.github.liyibo1110.resilience4j.core.registry;

/**
 * @author liyibo
 * @date 2026-02-04 14:45
 */
public class EntryAddedEvent<E> extends AbstractRegistryEvent {
    private E addedEntry;

    EntryAddedEvent(E addedEntry) {
        this.addedEntry = addedEntry;
    }

    @Override
    public Type getEventType() {
        return Type.ADDED;
    }

    public E getAddedEntry() {
        return this.addedEntry;
    }
}
