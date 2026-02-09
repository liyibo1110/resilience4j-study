package com.github.liyibo1110.resilience4j.cache.event;

/**
 * @author liyibo
 * @date 2026-02-09 14:26
 */
public class CacheOnMissEvent<K> extends AbstractCacheEvent {

    private final K cacheKey;

    public CacheOnMissEvent(String cacheName, K cacheKey) {
        super(cacheName);
        this.cacheKey = cacheKey;
    }

    @Override
    public Type getEventType() {
        return Type.CACHE_MISS;
    }

    public K getCacheKey() {
        return cacheKey;
    }

    @Override
    public String toString() {
        return String.format("%s: Cache '%s' recorded a cache miss on cache key '%s'.",
                getCreationTime(), getCacheName(), getCacheKey());
    }
}
