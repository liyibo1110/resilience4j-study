package com.github.liyibo1110.resilience4j.circuitbreaker.event;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;

/**
 * @author liyibo
 * @date 2026-02-05 13:55
 */
public class CircuitBreakerOnStateTransitionEvent extends AbstractCircuitBreakerEvent {
    private CircuitBreaker.StateTransition stateTransition;

    public CircuitBreakerOnStateTransitionEvent(String circuitBreakerName, CircuitBreaker.StateTransition stateTransition) {
        super(circuitBreakerName);
        this.stateTransition = stateTransition;
    }

    public CircuitBreaker.StateTransition getStateTransition() {
        return this.stateTransition;
    }

    @Override
    public Type getEventType() {
        return Type.STATE_TRANSITION;
    }

    @Override
    public String toString() {
        return String.format("%s: CircuitBreaker '%s' changed state from %s to %s",
                this.getCreationTime(), this.getCircuitBreakerName(),
                this.getStateTransition().getFromState(), this.getStateTransition().getToState());
    }
}
