package com.github.liyibo1110.resilience4j.circuitbreaker.event;

/**
 * @author liyibo
 * @date 2026-02-05 13:52
 */
public class CircuitBreakerOnResetEvent extends AbstractCircuitBreakerEvent {

    public CircuitBreakerOnResetEvent(String circuitBreakerName) {
        super(circuitBreakerName);
    }

    @Override
    public Type getEventType() {
        return Type.RESET;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' reset",
                this.getCreationTime(), this.getCircuitBreakerName());
    }
}
