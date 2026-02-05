package com.github.liyibo1110.resilience4j.circuitbreaker.event;

import java.time.Duration;

/**
 * @author liyibo
 * @date 2026-02-05 13:53
 */
public class CircuitBreakerOnIgnoredErrorEvent extends AbstractCircuitBreakerEvent {
    private final Duration elapsedDuration;
    private final Throwable throwable;

    public CircuitBreakerOnIgnoredErrorEvent(String circuitBreakerName, Duration elapsedDuration, Throwable throwable) {
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
        return Type.IGNORED_ERROR;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' recorded an error which has been ignored: '%s'. Elapsed time: %s ms",
                this.getCreationTime(), this.getCircuitBreakerName(), this.getThrowable(), this.getElapsedDuration().toMillis());
    }
}
