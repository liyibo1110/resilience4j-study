package com.github.liyibo1110.resilience4j.bulkhead.configure;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author liyibo
 * @date 2026-02-13 10:54
 */
public class ReactorBulkheadAspectExt implements BulkheadAspectExt {
    private static final Logger logger = LoggerFactory.getLogger(ReactorBulkheadAspectExt.class);


    @Override
    public boolean canHandleReturnType(Class returnType) {
        return Flux.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);
    }

    @Override
    public Object handle(ProceedingJoinPoint joinPoint, Bulkhead bulkhead, String methodName) throws Throwable {
        Object returnValue = joinPoint.proceed();
        if(Flux.class.isAssignableFrom(returnValue.getClass())) {
            Flux<?> fluxReturnValue = (Flux<?>)returnValue;
            return fluxReturnValue.transformDeferred(BulkheadOperator.of(bulkhead));
        }else if (Mono.class.isAssignableFrom(returnValue.getClass())) {
            Mono<?> monoReturnValue = (Mono<?>)returnValue;
            return monoReturnValue.transformDeferred(BulkheadOperator.of(bulkhead));
        }else {
            logger.error("Unsupported type for Reactor BulkHead {}", returnValue.getClass().getTypeName());
            throw new IllegalArgumentException("Not Supported type for the BulkHead in Reactor :" + returnValue.getClass().getName());
        }
    }
}
