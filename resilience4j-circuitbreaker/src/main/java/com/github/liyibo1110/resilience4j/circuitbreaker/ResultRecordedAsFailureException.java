package com.github.liyibo1110.resilience4j.circuitbreaker;

/**
 * 用来表示result已被record为cb的故障的异常
 * @author liyibo
 * @date 2026-02-05 16:20
 */
public class ResultRecordedAsFailureException extends RuntimeException {
    private final String circuitBreakerName;
    private final transient Object result;

    public ResultRecordedAsFailureException(String circuitBreakerName, Object result) {
        super(String.format("CircuitBreaker '%s' has recorded '%s' as a failure", circuitBreakerName, result.toString()));
        this.circuitBreakerName = circuitBreakerName;
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public String getCircuitBreakerName() {
        return circuitBreakerName;
    }
}
