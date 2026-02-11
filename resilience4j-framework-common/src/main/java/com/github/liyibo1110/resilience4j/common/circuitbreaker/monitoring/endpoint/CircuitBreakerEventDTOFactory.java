package com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnCallNotPermittedEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnFailureRateExceededEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnIgnoredErrorEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnResetEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnSlowCallRateExceededEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnSuccessEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:34
 */
public class CircuitBreakerEventDTOFactory {

    private CircuitBreakerEventDTOFactory() {}

    public static CircuitBreakerEventDTO createCircuitBreakerEventDTO(CircuitBreakerEvent event) {
        switch (event.getEventType()) {
            case ERROR:
                CircuitBreakerOnErrorEvent onErrorEvent = (CircuitBreakerOnErrorEvent) event;
                return newCircuitBreakerEventDTOBuilder(onErrorEvent)
                        .setThrowable(onErrorEvent.getThrowable())
                        .setDuration(onErrorEvent.getElapsedDuration())
                        .build();
            case SUCCESS:
                CircuitBreakerOnSuccessEvent onSuccessEvent = (CircuitBreakerOnSuccessEvent)event;
                return newCircuitBreakerEventDTOBuilder(onSuccessEvent)
                        .setDuration(onSuccessEvent.getElapsedDuration())
                        .build();
            case STATE_TRANSITION:
                CircuitBreakerOnStateTransitionEvent onStateTransitionEvent = (CircuitBreakerOnStateTransitionEvent)event;
                return newCircuitBreakerEventDTOBuilder(onStateTransitionEvent)
                        .setStateTransition(onStateTransitionEvent.getStateTransition())
                        .build();
            case RESET:
                CircuitBreakerOnResetEvent onResetEvent = (CircuitBreakerOnResetEvent)event;
                return newCircuitBreakerEventDTOBuilder(onResetEvent)
                        .build();
            case IGNORED_ERROR:
                CircuitBreakerOnIgnoredErrorEvent onIgnoredErrorEvent = (CircuitBreakerOnIgnoredErrorEvent)event;
                return newCircuitBreakerEventDTOBuilder(onIgnoredErrorEvent)
                        .setThrowable(onIgnoredErrorEvent.getThrowable())
                        .setDuration(onIgnoredErrorEvent.getElapsedDuration())
                        .build();
            case NOT_PERMITTED:
                CircuitBreakerOnCallNotPermittedEvent onCallNotPermittedEvent = (CircuitBreakerOnCallNotPermittedEvent)event;
                return newCircuitBreakerEventDTOBuilder(onCallNotPermittedEvent)
                        .build();
            case FAILURE_RATE_EXCEEDED:
                CircuitBreakerOnFailureRateExceededEvent onFailureRateExceededEvent = (CircuitBreakerOnFailureRateExceededEvent)event;
                return newCircuitBreakerEventDTOBuilder(onFailureRateExceededEvent)
                        .build();
            case SLOW_CALL_RATE_EXCEEDED:
                CircuitBreakerOnSlowCallRateExceededEvent onSlowCallRateExceededEvent = (CircuitBreakerOnSlowCallRateExceededEvent)event;
                return newCircuitBreakerEventDTOBuilder(onSlowCallRateExceededEvent)
                        .build();
            default:
                throw new IllegalArgumentException("Invalid event");
        }
    }

    private static CircuitBreakerEventDTOBuilder newCircuitBreakerEventDTOBuilder(CircuitBreakerEvent event) {
        return new CircuitBreakerEventDTOBuilder(event.getCircuitBreakerName(), event.getEventType(), event.getCreationTime().toString());
    }
}
