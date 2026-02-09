package com.github.liyibo1110.resilience4j.cache.event;

/**
 * @author liyibo
 * @date 2026-02-09 14:26
 */
public class CacheOnHitEvent<K> extends AbstractCacheEvent {

    private final K cacheKey;

    public CacheOnHitEvent(String cacheName, K cacheKey) {
        super(cacheName);
        this.cacheKey = cacheKey;
    }

    @Override
    public Type getEventType() {
        return Type.CACHE_HIT;
    }

    public K getCacheKey() {
        return cacheKey;
    }

    @Override
    public String toString() {
        return String.format("%s: Cache '%s' recorded a cache hit on cache key '%s'.",
                getCreationTime(), getCacheName(), getCacheKey());
    }
}
