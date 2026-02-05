package com.github.liyibo1110.resilience4j.circuitbreaker;

/**
 * 方法不允许被调用的相关异常
 * @author liyibo
 * @date 2026-02-05 14:16
 */
public class CallNotPermittedException extends RuntimeException {
    private final transient String causingCircuitBreakerName;

    private CallNotPermittedException(CircuitBreaker cb, String message, boolean writableStackTrace) {
        super(message, null, false, writableStackTrace);
        this.causingCircuitBreakerName = cb.getName();
    }

    /**
     * 实际可用的对象工厂
     */
    public static CallNotPermittedException createCallNotPermittedException(CircuitBreaker cb) {
        boolean writableStackTraceEnabled = cb.getCircuitBreakerConfig().isWritableStackTraceEnabled();
        String message = String.format("CircuitBreaker '%s' is %s and does not permit further calls", cb.getName(), cb.getState());
        return new CallNotPermittedException(cb, message, writableStackTraceEnabled);
    }

    public String getCausingCircuitBreakerName() {
        return this.causingCircuitBreakerName;
    }
}
