package com.github.liyibo1110.resilience4j.circuitbreaker.event;

import java.time.Duration;

/**
 * @author liyibo
 * @date 2026-02-05 13:50
 */
public class CircuitBreakerOnErrorEvent extends AbstractCircuitBreakerEvent {
    private final Duration elapsedDuration;
    private final Throwable throwable;

    public CircuitBreakerOnErrorEvent(String circuitBreakerName, Duration elapsedDuration, Throwable throwable) {
        super(circuitBreakerName);
        this.elapsedDuration = elapsedDuration;
        this.throwable = throwable;
    }

    public Duration getElapsedDuration() {
        return this.elapsedDuration;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public Type getEventType() {
        return Type.ERROR;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' recorded an error: '%s'. Elapsed time: %s ms",
                this.getCreationTime(), this.getCircuitBreakerName(), this.getThrowable(), this.getElapsedDuration().toMillis());
    }
}
