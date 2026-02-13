package com.github.liyibo1110.resilience4j.ratelimiter.configure;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author liyibo
 * @date 2026-02-13 10:56
 */
public interface RateLimiterAspectExt {

    boolean canHandleReturnType(Class returnType);

    Object handle(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter, String methodName) throws Throwable;
}
