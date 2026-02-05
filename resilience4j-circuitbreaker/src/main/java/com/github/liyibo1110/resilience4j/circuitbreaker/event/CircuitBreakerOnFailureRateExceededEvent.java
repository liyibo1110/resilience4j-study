package com.github.liyibo1110.resilience4j.circuitbreaker.event;

/**
 * @author liyibo
 * @date 2026-02-05 13:58
 */
public class CircuitBreakerOnFailureRateExceededEvent extends AbstractCircuitBreakerEvent {
    private final float failureRate;

    public CircuitBreakerOnFailureRateExceededEvent(String circuitBreakerName, float failureRate) {
        super(circuitBreakerName);
        this.failureRate = failureRate;
    }

    public float getFailureRate() {
        return this.failureRate;
    }

    @Override
    public Type getEventType() {
        return Type.FAILURE_RATE_EXCEEDED;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' exceeded failure rate threshold. Current failure rate: %s",
                this.getCreationTime(), this.getCircuitBreakerName(), this.getFailureRate());
    }
}
