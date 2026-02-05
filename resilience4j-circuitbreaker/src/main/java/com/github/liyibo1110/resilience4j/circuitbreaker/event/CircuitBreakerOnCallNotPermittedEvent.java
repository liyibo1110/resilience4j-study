package com.github.liyibo1110.resilience4j.circuitbreaker.event;

/**
 * @author liyibo
 * @date 2026-02-05 13:53
 */
public class CircuitBreakerOnCallNotPermittedEvent extends AbstractCircuitBreakerEvent {

    public CircuitBreakerOnCallNotPermittedEvent(String circuitBreakerName) {
        super(circuitBreakerName);
    }

    @Override
    public Type getEventType() {
        return Type.NOT_PERMITTED;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' recorded a call which was not permitted.",
                this.getCreationTime(), this.getCircuitBreakerName());
    }
}
