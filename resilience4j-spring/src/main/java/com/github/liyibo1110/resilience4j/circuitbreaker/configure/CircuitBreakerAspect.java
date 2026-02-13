package com.github.liyibo1110.resilience4j.circuitbreaker.configure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.FallbackMethod;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.utils.AnnotationExtractor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * @author liyibo
 * @date 2026-02-11 15:32
 */
@Aspect
public class CircuitBreakerAspect implements Ordered {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerAspect.class);
    private final CircuitBreakerConfigurationProperties prop;
    private final CircuitBreakerRegistry cbr;
    private final @Nullable List<CircuitBreakerAspectExt> circuitBreakerAspectExtList;
    private final FallbackDecorators fallbackDecorators;
    private final SpelResolver spelResolver;

    public CircuitBreakerAspect(CircuitBreakerConfigurationProperties prop,
                                CircuitBreakerRegistry cbr,
                                @Autowired(required=false) List<CircuitBreakerAspectExt> circuitBreakerAspectExtList,
                                FallbackDecorators fallbackDecorators,
                                SpelResolver spelResolver) {
        this.prop = prop;
        this.cbr = cbr;
        this.circuitBreakerAspectExtList = circuitBreakerAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
        this.spelResolver = spelResolver;
    }

    /**
     * 标记方法，即AOP增强所有带CircuitBreaker注解的方法
     */
    @Pointcut(value="@within(circuitBreaker) || @annotation(circuitBreaker)", argNames="circuitBreaker")
    public void matchAnnotatedClassOrMethod(CircuitBreaker circuitBreaker) {}

    @Around(value="matchAnnotatedClassOrMethod(circuitBreakerAnnotation)", argNames="joinPoint, circuitBreakerAnnotation")
    public Object circuitBreakerAroundAdvice(ProceedingJoinPoint joinPoint,
                                             @Nullable CircuitBreaker circuitBreakerAnnotation) throws Throwable {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        // 类名#方法名，不过只是用来打印log使用
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if(circuitBreakerAnnotation == null)
            circuitBreakerAnnotation = this.getCircuitBreakerAnnotation(joinPoint);
        if(circuitBreakerAnnotation == null)    // 如果还是没找到CircuitBreaker注解，只能直接执行完事
            return joinPoint.proceed();
        // 获取cb的实例name
        String backend = spelResolver.resolve(method, joinPoint.getArgs(), circuitBreakerAnnotation.name());
        com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker cb = getOrCreateCircuitBreaker(methodName, backend);
        Class<?> returnType = method.getReturnType();

        String fallbackMethodValue = spelResolver.resolve(method, joinPoint.getArgs(), circuitBreakerAnnotation.fallbackMethod());
        if(StringUtils.isEmpty(fallbackMethodValue))    // 没有fallback，就走cb增强方案
            return this.proceed(joinPoint, methodName, cb, returnType);
        // 有fallback，就走cb + fallback增强方案
        FallbackMethod fallbackMethod = FallbackMethod.create(fallbackMethodValue, method, joinPoint.getArgs(), joinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod, () -> this.proceed(joinPoint, methodName, cb, returnType)).apply();
    }

    /**
     * 执行cb增强（fallback增强在外层处理，不在这里）
     */
    private Object proceed(ProceedingJoinPoint joinPoint, String methodName,
                           com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker cb,
                           Class<?> returnType) throws Throwable {
        // 专门针对Reactor或RxJava的AOP增强方案，执行这里面的，就不在执行后面的其它增强流程了
        if(circuitBreakerAspectExtList != null && !circuitBreakerAspectExtList.isEmpty()) {
            for(CircuitBreakerAspectExt cbe : circuitBreakerAspectExtList) {
                if(cbe.canHandleReturnType(returnType))
                    return cbe.handle(joinPoint, cb, methodName);
            }
        }
        if(CompletionStage.class.isAssignableFrom(returnType))
            return handleJoinPointCompletableFuture(joinPoint, cb);
        return defaultHandling(joinPoint, cb);
    }

    /**
     * 根据注解的name，创建cb实例
     */
    private com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker getOrCreateCircuitBreaker(
        String methodName, String backend) {
        com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker cb = cbr.circuitBreaker(backend);
        if(logger.isDebugEnabled())
            logger.debug("Created or retrieved circuit breaker '{}' with failure rate '{}' for method: '{}'",
                    backend, cb.getCircuitBreakerConfig().getFailureRateThreshold(),
                    methodName);
        return cb;
    }


    @Nullable
    private CircuitBreaker getCircuitBreakerAnnotation(ProceedingJoinPoint joinPoint) {
        if(logger.isDebugEnabled())
            logger.debug("circuitBreaker parameter is null");
        if(joinPoint.getTarget() instanceof Proxy) {
            logger.debug("The circuit breaker annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor.extractAnnotationFromProxy(joinPoint.getTarget(), CircuitBreaker.class);
        }else {
            return AnnotationExtractor.extract(joinPoint.getTarget().getClass(), CircuitBreaker.class);
        }
    }

    /**
     * 返回值是CompletionStage及其子类，则走这个最终处理
     */
    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint joinPoint,
                                                    com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker cb) {
        return cb.executeCompletionStage(() -> {
            try {
                return (CompletionStage<?>)joinPoint.proceed();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        });
    }

    /**
     * 返回值不是CompletionStage及其子类，则走这个最终处理
     */
    private Object defaultHandling(ProceedingJoinPoint joinPoint,
                                   com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker cb) throws Throwable {
        return cb.executeCheckedSupplier(joinPoint::proceed);
    }

    @Override
    public int getOrder() {
        return prop.getCircuitBreakerAspectOrder();
    }
}
