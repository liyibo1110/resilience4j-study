package com.github.liyibo1110.resilience4j.circuitbreaker.event;

/**
 * @author liyibo
 * @date 2026-02-05 13:56
 */
public class CircuitBreakerOnSlowCallRateExceededEvent extends AbstractCircuitBreakerEvent {

    private final float slowCallRate;

    public CircuitBreakerOnSlowCallRateExceededEvent(String circuitBreakerName, float slowCallRate) {
        super(circuitBreakerName);
        this.slowCallRate = slowCallRate;
    }

    public float getSlowCallRate() {
        return this.slowCallRate;
    }

    @Override
    public Type getEventType() {
        return Type.SLOW_CALL_RATE_EXCEEDED;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' exceeded slow call rate threshold. Current slow call rate: %s",
                this.getCreationTime(), this.getCircuitBreakerName(), this.getSlowCallRate());
    }
}
