package com.github.liyibo1110.resilience4j.ratelimiter.configure;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.FallbackMethod;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.annotation.RateLimiter;
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
 * @date 2026-02-13 11:09
 */
@Aspect
public class RateLimiterAspect implements Ordered {
    private static final String RATE_LIMITER_RECEIVED = "Created or retrieved rate limiter '{}' with period: '{}'; limit for period: '{}'; timeout: '{}'; method: '{}'";
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiterConfigurationProperties prop;
    private final @Nullable List<RateLimiterAspectExt> rateLimiterAspectExtList;
    private final FallbackDecorators fallbackDecorators;
    private final SpelResolver spelResolver;

    public RateLimiterAspect(RateLimiterRegistry rateLimiterRegistry,
                             RateLimiterConfigurationProperties prop,
                             @Autowired(required=false) List<RateLimiterAspectExt> rateLimiterAspectExtList,
                             FallbackDecorators fallbackDecorators,
                             SpelResolver spelResolver) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.prop = prop;
        this.rateLimiterAspectExtList = rateLimiterAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
        this.spelResolver = spelResolver;
    }

    @Pointcut(value="@within(rateLimiter) || @annotation(rateLimiter)", argNames="rateLimiter")
    public void matchAnnotatedClassOrMethod(RateLimiter rateLimiter) {}

    @Around(value="matchAnnotatedClassOrMethod(rateLimiterAnnotation)", argNames="joinPoint, rateLimiterAnnotation")
    public Object rateLimiterAroundAdvice(ProceedingJoinPoint joinPoint, @Nullable RateLimiter rateLimiterAnnotation) throws Throwable {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if(rateLimiterAnnotation == null)
            rateLimiterAnnotation = getRateLimiterAnnotation(joinPoint);

        if(rateLimiterAnnotation == null)
            return joinPoint.proceed();

        String name = spelResolver.resolve(method, joinPoint.getArgs(), rateLimiterAnnotation.name());
        com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter rateLimiter = getOrCreateRateLimiter(methodName, name);
        Class<?> returnType = method.getReturnType();

        String fallbackMethodValue = spelResolver.resolve(method, joinPoint.getArgs(), rateLimiterAnnotation.fallbackMethod());
        if(StringUtils.isEmpty(fallbackMethodValue))
            return proceed(joinPoint, methodName, returnType, rateLimiter);

        FallbackMethod fallbackMethod = FallbackMethod.create(fallbackMethodValue, method, joinPoint.getArgs(), joinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod, () -> proceed(joinPoint, methodName, returnType, rateLimiter)).apply();
    }

    private Object proceed(ProceedingJoinPoint joinPoint, String methodName, Class<?> returnType,
                           com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter rateLimiter) throws Throwable {
        if(rateLimiterAspectExtList != null && !rateLimiterAspectExtList.isEmpty()) {
            for(RateLimiterAspectExt rateLimiterAspectExt : rateLimiterAspectExtList) {
                if(rateLimiterAspectExt.canHandleReturnType(returnType))
                    return rateLimiterAspectExt.handle(joinPoint, rateLimiter, methodName);
            }
        }
        if(CompletionStage.class.isAssignableFrom(returnType))
            return handleJoinPointCompletableFuture(joinPoint, rateLimiter);
        return handleJoinPoint(joinPoint, rateLimiter);
    }

    private com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter getOrCreateRateLimiter(String methodName, String name) {
        com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(name);
        if(logger.isDebugEnabled()) {
            RateLimiterConfig rateLimiterConfig = rateLimiter.getRateLimiterConfig();
            logger.debug(RATE_LIMITER_RECEIVED, name, rateLimiterConfig.getLimitRefreshPeriod(),
                    rateLimiterConfig.getLimitForPeriod(), rateLimiterConfig.getTimeoutDuration(), methodName
            );
        }
        return rateLimiter;
    }

    @Nullable
    private RateLimiter getRateLimiterAnnotation(ProceedingJoinPoint joinPoint) {
        if(joinPoint.getTarget() instanceof Proxy) {
            logger.debug("The rate limiter annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor.extractAnnotationFromProxy(joinPoint.getTarget(), RateLimiter.class);
        }else {
            return AnnotationExtractor.extract(joinPoint.getTarget().getClass(), RateLimiter.class);
        }
    }

    private Object handleJoinPoint(ProceedingJoinPoint joinPoint,
                                   com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter rateLimiter) throws Throwable {
        return rateLimiter.executeCheckedSupplier(joinPoint::proceed);
    }

    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint joinPoint,
                                                    com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter rateLimiter) {
        return rateLimiter.executeCompletionStage(() -> {
            try {
                return (CompletionStage<?>) joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }

    @Override
    public int getOrder() {
        return prop.getRateLimiterAspectOrder();
    }
}
