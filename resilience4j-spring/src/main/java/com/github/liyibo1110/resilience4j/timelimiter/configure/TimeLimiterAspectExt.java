package com.github.liyibo1110.resilience4j.timelimiter.configure;

import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author liyibo
 * @date 2026-02-13 10:59
 */
public interface TimeLimiterAspectExt {

    boolean canHandleReturnType(Class<?> returnType);

    Object handle(ProceedingJoinPoint joinPoint, TimeLimiter timeLimiter, String methodName) throws Throwable;
}
