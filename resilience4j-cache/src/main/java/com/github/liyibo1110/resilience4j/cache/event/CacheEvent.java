package com.github.liyibo1110.resilience4j.cache.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-09 14:12
 */
public interface CacheEvent {
    String getCacheName();
    ZonedDateTime getCreationTime();
    Type getEventType();

    enum Type {
        ERROR,  // cache不可用
        CACHE_HIT,  // cache命中
        CACHE_MISS  // cache未命中
    }
}
