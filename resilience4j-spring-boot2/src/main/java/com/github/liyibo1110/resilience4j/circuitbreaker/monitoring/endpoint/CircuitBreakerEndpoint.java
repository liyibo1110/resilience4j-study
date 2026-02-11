package com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint.CircuitBreakerEndpointResponse;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint.CircuitBreakerUpdateStateResponse;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint.UpdateState;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.Arrays;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 16:54
 */
@Endpoint(id="circuitbreakers")
public class CircuitBreakerEndpoint {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerEndpoint(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @ReadOperation
    public CircuitBreakerEndpointResponse getAllCircuitBreakers() {
        // 组件的name
        List<String> circuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers()
                .map(CircuitBreaker::getName).sorted().toJavaList();
        return new CircuitBreakerEndpointResponse(circuitBreakers);
    }

    @WriteOperation
    public CircuitBreakerUpdateStateResponse updateCircuitBreakerState(@Selector String name, UpdateState updateState) {
        final CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        final String message = "%s state has been changed successfully";
        return switch(updateState) {
            case CLOSE -> {
                circuitBreaker.transitionToClosedState();
                yield createCircuitBreakerUpdateStateResponse(name, circuitBreaker.getState().toString(), String.format(message, name));
            }
            case FORCE_OPEN -> {
                circuitBreaker.transitionToForcedOpenState();
                yield createCircuitBreakerUpdateStateResponse(name, circuitBreaker.getState().toString(), String.format(message, name));
            }
            case DISABLE -> {
                circuitBreaker.transitionToDisabledState();
                yield createCircuitBreakerUpdateStateResponse(name, circuitBreaker.getState().toString(), String.format(message, name));
            }
            default -> createCircuitBreakerUpdateStateResponse(name, circuitBreaker.getState().toString(), "State change value is not supported please use only " + Arrays.toString(UpdateState.values()));
        };
    }

    private CircuitBreakerUpdateStateResponse createCircuitBreakerUpdateStateResponse(String circuitBreakerName, String newState, String message) {
        CircuitBreakerUpdateStateResponse response = new CircuitBreakerUpdateStateResponse();
        response.setCircuitBreakerName(circuitBreakerName);
        response.setCurrentState(newState);
        response.setMessage(message);
        return response;
    }
}
