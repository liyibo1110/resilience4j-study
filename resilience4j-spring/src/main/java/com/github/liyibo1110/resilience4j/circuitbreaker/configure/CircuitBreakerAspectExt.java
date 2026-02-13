package com.github.liyibo1110.resilience4j.circuitbreaker.configure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * CircuitBreaker切面的扩展，以支持新的类型（其实就是Reactor和RxJava）
 * @author liyibo
 * @date 2026-02-13 00:31
 */
public interface CircuitBreakerAspectExt {

    boolean canHandleReturnType(Class returnType);

    Object handle(ProceedingJoinPoint joinPoint, CircuitBreaker cb, String methodName) throws Throwable;
}
