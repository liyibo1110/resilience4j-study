package com.github.liyibo1110.resilience4j.circuitbreaker.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-05 11:45
 */
public interface CircuitBreakerEvent {

    String getCircuitBreakerName();

    Type getEventType();

    ZonedDateTime getCreationTime();

    enum Type {
        ERROR(false),
        IGNORED_ERROR(false),
        SUCCESS(false),
        NOT_PERMITTED(false),
        STATE_TRANSITION(true),
        RESET(true),
        FORCED_OPEN(false),
        DISABLED(false),
        FAILURE_RATE_EXCEEDED(false),
        SLOW_CALL_RATE_EXCEEDED(false);

        public final boolean forcePublish;

        Type(boolean forcePublish) {
            this.forcePublish = forcePublish;
        }
    }
}
