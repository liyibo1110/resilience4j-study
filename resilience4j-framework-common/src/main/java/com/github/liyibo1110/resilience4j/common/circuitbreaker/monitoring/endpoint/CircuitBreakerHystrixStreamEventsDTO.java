package com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:40
 */
public class CircuitBreakerHystrixStreamEventsDTO {
    private CircuitBreakerEvent circuitBreakerRecentEvent;
    private CircuitBreaker.Metrics metrics;
    private CircuitBreaker.State currentState;
    private float failureRateThreshold;
    private float slowCallRateThreshold;

    public CircuitBreakerHystrixStreamEventsDTO(CircuitBreakerEvent circuitBreakerEvent, CircuitBreaker.State state,
                                                CircuitBreaker.Metrics metrics, CircuitBreakerConfig circuitBreakerConfig) {
        this.circuitBreakerRecentEvent = circuitBreakerEvent;
        this.metrics = metrics;
        this.currentState = state;
        this.failureRateThreshold = circuitBreakerConfig.getFailureRateThreshold();
        this.slowCallRateThreshold = circuitBreakerConfig.getSlowCallRateThreshold();
    }

    public CircuitBreakerEvent getCircuitBreakerRecentEvent() {
        return circuitBreakerRecentEvent;
    }

    public void setCircuitBreakerRecentEvent(CircuitBreakerEvent circuitBreakerRecentEvent) {
        this.circuitBreakerRecentEvent = circuitBreakerRecentEvent;
    }

    public CircuitBreaker.Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(CircuitBreaker.Metrics metrics) {
        this.metrics = metrics;
    }

    public CircuitBreaker.State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CircuitBreaker.State currentState) {
        this.currentState = currentState;
    }


    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(float failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public float getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public void setSlowCallRateThreshold(float slowCallRateThreshold) {
        this.slowCallRateThreshold = slowCallRateThreshold;
    }
}
