package com.github.liyibo1110.resilience4j.core.registry;

import java.time.ZonedDateTime;

/**
 * Registry组件相关的event
 * @author liyibo
 * @date 2026-02-04 14:24
 */
public interface RegistryEvent {
    /** event的类型 */
    Type getEventType();

    /** event的创建时间 */
    ZonedDateTime getCreationTime();

    /**
     * event类型（CircuitBreaker模块相关功能会使用）
     */
    enum Type {
        /** 新增entry */
        ADDED,
        /** 移除entry */
        REMOVED,
        /** 替换entry */
        REPLACED
    }
}
