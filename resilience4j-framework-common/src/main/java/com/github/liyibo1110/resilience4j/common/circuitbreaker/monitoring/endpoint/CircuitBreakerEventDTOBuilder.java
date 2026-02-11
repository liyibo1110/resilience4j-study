package com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.time.Duration;

/**
 * @author liyibo
 * @date 2026-02-10 00:33
 */
class CircuitBreakerEventDTOBuilder {
    private final String circuitBreakerName;
    private final CircuitBreakerEvent.Type type;
    private final String creationTime;
    @Nullable
    private String throwable = null;
    @Nullable
    private Long duration = null;
    @Nullable
    private CircuitBreaker.StateTransition stateTransition = null;

    CircuitBreakerEventDTOBuilder(String circuitBreakerName, CircuitBreakerEvent.Type type, String creationTime) {
        this.circuitBreakerName = circuitBreakerName;
        this.type = type;
        this.creationTime = creationTime;
    }

    CircuitBreakerEventDTOBuilder setThrowable(Throwable throwable) {
        this.throwable = throwable.toString();
        return this;
    }

    CircuitBreakerEventDTOBuilder setDuration(Duration duration) {
        this.duration = duration.toMillis();
        return this;
    }

    CircuitBreakerEventDTOBuilder setStateTransition(
            CircuitBreaker.StateTransition stateTransition) {
        this.stateTransition = stateTransition;
        return this;
    }

    CircuitBreakerEventDTO build() {
        return new CircuitBreakerEventDTO(circuitBreakerName, type, creationTime, throwable, duration, stateTransition);
    }
}
