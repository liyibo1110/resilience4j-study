package com.github.liyibo1110.resilience4j.circuitbreaker;

/**
 * cb的状态切换相关异常
 * @author liyibo
 * @date 2026-02-05 11:22
 */
public class IllegalStateTransitionException extends RuntimeException {
    private final String name;
    private final CircuitBreaker.State fromState;
    private final CircuitBreaker.State toState;

    IllegalStateTransitionException(String name, CircuitBreaker.State fromState,
                                    CircuitBreaker.State toState) {
        super(String.format("CircuitBreaker '%s' tried an illegal state transition from %s to %s",
                name, fromState, toState));
        this.name = name;
        this.fromState = fromState;
        this.toState = toState;
    }

    public CircuitBreaker.State getFromState() {
        return fromState;
    }

    public CircuitBreaker.State getToState() {
        return toState;
    }

    public String getName() {
        return name;
    }
}
