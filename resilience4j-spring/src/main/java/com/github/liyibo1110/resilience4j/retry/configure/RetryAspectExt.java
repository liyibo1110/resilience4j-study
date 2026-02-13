package com.github.liyibo1110.resilience4j.retry.configure;

import com.github.liyibo1110.resilience4j.retry.Retry;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author liyibo
 * @date 2026-02-13 10:57
 */
public interface RetryAspectExt {

    boolean canHandleReturnType(Class returnType);

    Object handle(ProceedingJoinPoint joinPoint, Retry retry, String methodName) throws Throwable;
}
