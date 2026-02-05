package com.github.liyibo1110.resilience4j.circuitbreaker.event;

import java.time.Duration;

/**
 * @author liyibo
 * @date 2026-02-05 11:51
 */
public class CircuitBreakerOnSuccessEvent extends AbstractCircuitBreakerEvent {

    private final Duration elapsedDuration;

    public CircuitBreakerOnSuccessEvent(String circuitBreakerName, Duration elapsedDuration) {
        super(circuitBreakerName);
        this.elapsedDuration = elapsedDuration;
    }

    public Duration getElapsedDuration() {
        return this.elapsedDuration;
    }

    @Override
    public Type getEventType() {
        return Type.SUCCESS;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' recorded a successful call. Elapsed time: %s ms",
                this.getCreationTime(), this.getCircuitBreakerName(), this.getElapsedDuration().toMillis());
    }
}
