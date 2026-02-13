package com.github.liyibo1110.resilience4j.retry.configure;

import com.github.liyibo1110.resilience4j.reactor.retry.RetryOperator;
import com.github.liyibo1110.resilience4j.retry.Retry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author liyibo
 * @date 2026-02-13 10:58
 */
public class ReactorRetryAspectExt implements RetryAspectExt {
    private static final Logger logger = LoggerFactory.getLogger(ReactorRetryAspectExt.class);

    @Override
    public boolean canHandleReturnType(Class returnType) {
        return Flux.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);
    }

    @Override
    public Object handle(ProceedingJoinPoint joinPoint, Retry retry, String methodName) throws Throwable {
        Object returnValue = joinPoint.proceed();
        if(Flux.class.isAssignableFrom(returnValue.getClass())) {
            Flux<?> fluxReturnValue = (Flux<?>) returnValue;
            return fluxReturnValue.transformDeferred(RetryOperator.of(retry));
        }else if (Mono.class.isAssignableFrom(returnValue.getClass())) {
            Mono<?> monoReturnValue = (Mono<?>) returnValue;
            return monoReturnValue.transformDeferred(RetryOperator.of(retry));
        } else {
            logger.error("Unsupported type for Reactor retry {}", returnValue.getClass().getTypeName());
            throw new IllegalArgumentException("Not Supported type for the retry in Reactor :" + returnValue.getClass().getName());
        }
    }
}
