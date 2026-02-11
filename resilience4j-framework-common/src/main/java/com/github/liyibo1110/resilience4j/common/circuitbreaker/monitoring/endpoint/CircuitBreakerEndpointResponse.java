package com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:37
 */
public class CircuitBreakerEndpointResponse {
    @Nullable
    private List<String> circuitBreakers;

    public CircuitBreakerEndpointResponse() {}

    public CircuitBreakerEndpointResponse(List<String> circuitBreakers) {
        this.circuitBreakers = circuitBreakers;
    }

    @Nullable
    public List<String> getCircuitBreakers() {
        return circuitBreakers;
    }

    public void setCircuitBreakers(List<String> circuitBreakers) {
        this.circuitBreakers = circuitBreakers;
    }
}
