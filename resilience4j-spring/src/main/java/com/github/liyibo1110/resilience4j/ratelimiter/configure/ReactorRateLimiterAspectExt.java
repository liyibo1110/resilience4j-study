package com.github.liyibo1110.resilience4j.ratelimiter.configure;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author liyibo
 * @date 2026-02-13 10:56
 */
public class ReactorRateLimiterAspectExt implements RateLimiterAspectExt {
    private static final Logger logger = LoggerFactory.getLogger(ReactorRateLimiterAspectExt.class);

    @Override
    public boolean canHandleReturnType(Class returnType) {
        return Flux.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);
    }

    @Override
    public Object handle(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter, String methodName) throws Throwable {
        Object returnValue = joinPoint.proceed();
        if(Flux.class.isAssignableFrom(returnValue.getClass())) {
            Flux<?> fluxReturnValue = (Flux<?>) returnValue;
            return fluxReturnValue.transformDeferred(RateLimiterOperator.of(rateLimiter));
        }else if (Mono.class.isAssignableFrom(returnValue.getClass())) {
            Mono<?> monoReturnValue = (Mono<?>) returnValue;
            return monoReturnValue.transformDeferred(RateLimiterOperator.of(rateLimiter));
        }else {
            logger.error("Unsupported type for Reactor rateLimiter {}", returnValue.getClass().getTypeName());
            throw new IllegalArgumentException("Not Supported type for the rateLimiter in Reactor :" + returnValue.getClass().getName());
        }
    }
}
