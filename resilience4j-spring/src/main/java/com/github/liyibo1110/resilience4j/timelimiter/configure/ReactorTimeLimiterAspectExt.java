package com.github.liyibo1110.resilience4j.timelimiter.configure;

import com.github.liyibo1110.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author liyibo
 * @date 2026-02-13 11:00
 */
public class ReactorTimeLimiterAspectExt implements TimeLimiterAspectExt {
    @Override
    public boolean canHandleReturnType(Class<?> returnType) {
        return Flux.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);
    }

    @Override
    public Object handle(ProceedingJoinPoint joinPoint, TimeLimiter timeLimiter, String methodName) throws Throwable {
        Object returnValue = joinPoint.proceed();
        if(Flux.class.isAssignableFrom(returnValue.getClass())) {
            Flux<?> fluxReturnValue = (Flux<?>)returnValue;
            return fluxReturnValue.transformDeferred(TimeLimiterOperator.of(timeLimiter));
        }else if (Mono.class.isAssignableFrom(returnValue.getClass())) {
            Mono<?> monoReturnValue = (Mono<?>)returnValue;
            return monoReturnValue.transformDeferred(TimeLimiterOperator.of(timeLimiter));
        }else {
            throw new IllegalReturnTypeException(returnValue.getClass(), methodName, "Reactor expects Mono/Flux.");
        }
    }
}
