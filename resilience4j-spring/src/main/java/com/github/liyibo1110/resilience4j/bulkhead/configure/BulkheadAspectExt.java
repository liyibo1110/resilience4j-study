package com.github.liyibo1110.resilience4j.bulkhead.configure;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author liyibo
 * @date 2026-02-13 10:53
 */
public interface BulkheadAspectExt {

    boolean canHandleReturnType(Class returnType);

    Object handle(ProceedingJoinPoint joinPoint, Bulkhead bulkhead, String methodName) throws Throwable;
}
