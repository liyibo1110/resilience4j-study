package com.github.liyibo1110.resilience4j.core.registry;

/**
 * @author liyibo
 * @date 2026-02-04 14:46
 */
public class EntryRemovedEvent<E> extends AbstractRegistryEvent {
    private E removedEntry;

    EntryRemovedEvent(E removedEntry) {
        this.removedEntry = removedEntry;
    }

    @Override
    public Type getEventType() {
        return Type.REMOVED;
    }

    public E getRemovedEntry() {
        return this.removedEntry;
    }
}
