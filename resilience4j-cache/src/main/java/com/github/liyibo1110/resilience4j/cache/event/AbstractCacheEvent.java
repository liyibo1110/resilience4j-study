package com.github.liyibo1110.resilience4j.cache.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-09 14:24
 */
abstract class AbstractCacheEvent implements CacheEvent {
    private final String cacheName;
    private final ZonedDateTime creationTime;

    AbstractCacheEvent(String cacheName) {
        this.cacheName = cacheName;
        this.creationTime = ZonedDateTime.now();
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }
}
