package com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;

/**
 * @author liyibo
 * @date 2026-02-10 00:32
 */
public class CircuitBreakerEventDTO {
    @Nullable
    private String circuitBreakerName;
    @Nullable
    private CircuitBreakerEvent.Type type;
    @Nullable
    private String creationTime;
    @Nullable
    private String errorMessage;
    @Nullable
    private Long durationInMs;
    @Nullable
    private CircuitBreaker.StateTransition stateTransition;

    CircuitBreakerEventDTO() {}

    CircuitBreakerEventDTO(String circuitBreakerName, CircuitBreakerEvent.Type type,
                           String creationTime, @Nullable String errorMessage,
                           @Nullable Long durationInMs, @Nullable CircuitBreaker.StateTransition stateTransition) {
        this.circuitBreakerName = circuitBreakerName;
        this.type = type;
        this.creationTime = creationTime;
        this.errorMessage = errorMessage;
        this.durationInMs = durationInMs;
        this.stateTransition = stateTransition;
    }

    @Nullable
    public String getCircuitBreakerName() {
        return circuitBreakerName;
    }

    public void setCircuitBreakerName(String circuitBreakerName) {
        this.circuitBreakerName = circuitBreakerName;
    }

    @Nullable
    public CircuitBreakerEvent.Type getType() {
        return type;
    }

    public void setType(CircuitBreakerEvent.Type type) {
        this.type = type;
    }

    @Nullable
    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Nullable
    public Long getDurationInMs() {
        return durationInMs;
    }

    public void setDurationInMs(Long durationInMs) {
        this.durationInMs = durationInMs;
    }

    @Nullable
    public CircuitBreaker.StateTransition getStateTransition() {
        return stateTransition;
    }

    public void setStateTransition(CircuitBreaker.StateTransition stateTransition) {
        this.stateTransition = stateTransition;
    }
}
