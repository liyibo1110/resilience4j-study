package com.github.liyibo1110.resilience4j.circuitbreaker.event;

import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-05 11:49
 */
abstract class AbstractCircuitBreakerEvent implements CircuitBreakerEvent {
    /** 不可变 */
    private static final Clock SYSTEM_CLOCK = Clock.systemDefaultZone();

    private final String circuitBreakerName;
    private final ZonedDateTime creationTime;

    AbstractCircuitBreakerEvent(String circuitBreakerName) {
        this.circuitBreakerName = circuitBreakerName;
        this.creationTime = ZonedDateTime.now(SYSTEM_CLOCK);
    }

    @Override
    public String getCircuitBreakerName() {
        return this.circuitBreakerName;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return this.creationTime;
    }
}
