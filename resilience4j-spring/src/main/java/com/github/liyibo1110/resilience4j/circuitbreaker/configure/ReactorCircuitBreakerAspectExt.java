package com.github.liyibo1110.resilience4j.circuitbreaker.configure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用来增强标记了@CircuitBreaker注解，但方法返回值是Flux或Mono的reactor方法
 * @author liyibo
 * @date 2026-02-13 01:01
 */
public class ReactorCircuitBreakerAspectExt implements CircuitBreakerAspectExt {
    private static final Logger logger = LoggerFactory.getLogger(ReactorCircuitBreakerAspectExt.class);

    @Override
    public boolean canHandleReturnType(Class returnType) {
        return Flux.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);
    }

    @Override
    public Object handle(ProceedingJoinPoint joinPoint, CircuitBreaker cb, String methodName) throws Throwable {
        Object returnValue = joinPoint.proceed();
        if(Flux.class.isAssignableFrom(returnValue.getClass())) {
            Flux<?> fluxReturnValue = (Flux<?>)returnValue;
            /**
             * 对于reactor，直接利用transformDeferred方法来装饰即可，
             * 至于transformDeferred和transform的区别，在于前者生成的最终Flux，每次调用subscribe时，会重新调用传进去的Operator，
             * 而不是共享同一个，即生效时机被推迟到subscribe时，而不是组装时
             */
            return fluxReturnValue.transformDeferred(
                    com.github.liyibo1110.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator.of(cb));
        }else if(Mono.class.isAssignableFrom(returnValue.getClass())) {
            Mono<?> monoReturnValue = (Mono<?>)returnValue;
            return monoReturnValue.transformDeferred(CircuitBreakerOperator.of(cb));
        }else {
            logger.error("Unsupported type for Reactor circuit breaker {}", returnValue.getClass().getTypeName());
            throw new IllegalArgumentException("Not Supported type for the circuit breaker in Reactor:" + returnValue.getClass().getName());
        }
    }
}
