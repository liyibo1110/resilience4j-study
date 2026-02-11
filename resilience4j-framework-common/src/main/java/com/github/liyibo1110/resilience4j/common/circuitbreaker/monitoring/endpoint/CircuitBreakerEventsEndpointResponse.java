package com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 0:38
 */
public class CircuitBreakerEventsEndpointResponse {
    @Nullable
    private List<CircuitBreakerEventDTO> circuitBreakerEvents;

    public CircuitBreakerEventsEndpointResponse() {}

    public CircuitBreakerEventsEndpointResponse(@Nullable List<CircuitBreakerEventDTO> circuitBreakerEvents) {
        this.circuitBreakerEvents = circuitBreakerEvents;
    }

    @Nullable
    public List<CircuitBreakerEventDTO> getCircuitBreakerEvents() {
        return circuitBreakerEvents;
    }

    public void setCircuitBreakerEvents(@Nullable List<CircuitBreakerEventDTO> circuitBreakerEvents) {
        this.circuitBreakerEvents = circuitBreakerEvents;
    }
}
