package com.github.liyibo1110.resilience4j.core.registry;

/**
 * @author liyibo
 * @date 2026-02-04 14:47
 */
public class EntryReplacedEvent<E> extends AbstractRegistryEvent {
    private final E oldEntry;
    private final E newEntry;

    EntryReplacedEvent(E oldEntry, E newEntry) {
        super();
        this.oldEntry = oldEntry;
        this.newEntry = newEntry;
    }

    @Override
    public Type getEventType() {
        return Type.REPLACED;
    }

    public E getOldEntry() {
        return oldEntry;
    }

    public E getNewEntry() {
        return newEntry;
    }
}
