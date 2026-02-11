package com.github.liyibo1110.resilience4j.circuitbreaker.configure;

import org.springframework.core.Ordered;

/**
 * @author liyibo
 * @date 2026-02-10 15:58
 */
public class CircuitBreakerConfigurationProperties extends com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties {
    private int circuitBreakerAspectOrder = Ordered.LOWEST_PRECEDENCE - 3;

    public int getCircuitBreakerAspectOrder() {
        return circuitBreakerAspectOrder;
    }

    public void setCircuitBreakerAspectOrder(int circuitBreakerAspectOrder) {
        this.circuitBreakerAspectOrder = circuitBreakerAspectOrder;
    }
}
