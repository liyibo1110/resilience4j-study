package com.github.liyibo1110.resilience4j.cache.event;

/**
 * @author liyibo
 * @date 2026-02-09 14:25
 */
public class CacheOnErrorEvent extends AbstractCacheEvent {

    private final Throwable throwable;

    public CacheOnErrorEvent(String cacheName, Throwable throwable) {
        super(cacheName);
        this.throwable = throwable;
    }

    @Override
    public Type getEventType() {
        return Type.ERROR;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return String.format("%s: Cache '%s' recorded an error: '%s'.",
                getCreationTime(), getCacheName(), getThrowable());
    }
}
