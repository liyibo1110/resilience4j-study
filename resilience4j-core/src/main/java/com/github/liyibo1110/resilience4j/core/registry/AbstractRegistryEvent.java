package com.github.liyibo1110.resilience4j.core.registry;

import java.time.ZonedDateTime;

/**
 * RegistryEvent的抽象实现，就是多了个creationTime
 * @author liyibo
 * @date 2026-02-04 14:43
 */
abstract class AbstractRegistryEvent implements RegistryEvent {
    private final ZonedDateTime creationTime;

    AbstractRegistryEvent() {
        this.creationTime = ZonedDateTime.now();
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return this.creationTime;
    }
}
