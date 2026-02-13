package com.github.liyibo1110.resilience4j.retry.configure;

import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.FallbackMethod;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import com.github.liyibo1110.resilience4j.retry.annotation.Retry;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.utils.AnnotationExtractor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author liyibo
 * @date 2026-02-13 11:16
 */
public class RetryAspect implements Ordered, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RetryAspect.class);
    private final ScheduledExecutorService retryExecutorService;
    private final RetryConfigurationProperties prop;
    private final RetryRegistry retryRegistry;
    private final @Nullable List<RetryAspectExt> retryAspectExtList;
    private final FallbackDecorators fallbackDecorators;
    private final SpelResolver spelResolver;

    public RetryAspect(RetryConfigurationProperties prop, RetryRegistry retryRegistry,
                       @Autowired(required = false) List<RetryAspectExt> retryAspectExtList,
                       FallbackDecorators fallbackDecorators, SpelResolver spelResolver,
                       @Nullable ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor) {
        this.prop = prop;
        this.retryRegistry = retryRegistry;
        this.retryAspectExtList = retryAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
        this.spelResolver = spelResolver;
        this.retryExecutorService = contextAwareScheduledThreadPoolExecutor != null
                ? contextAwareScheduledThreadPoolExecutor
                : Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Pointcut(value = "@within(retry) || @annotation(retry)", argNames="retry")
    public void matchAnnotatedClassOrMethod(Retry retry) {}

    @Around(value="matchAnnotatedClassOrMethod(retryAnnotation)", argNames="joinPoint, retryAnnotation")
    public Object retryAroundAdvice(ProceedingJoinPoint joinPoint, @Nullable Retry retryAnnotation) throws Throwable {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if(retryAnnotation == null)
            retryAnnotation = getRetryAnnotation(joinPoint);
        if(retryAnnotation == null)
            return joinPoint.proceed();

        String backend = spelResolver.resolve(method, joinPoint.getArgs(), retryAnnotation.name());
        com.github.liyibo1110.resilience4j.retry.Retry retry = getOrCreateRetry(methodName, backend);
        Class<?> returnType = method.getReturnType();

        String fallbackMethodValue = spelResolver.resolve(method, joinPoint.getArgs(), retryAnnotation.fallbackMethod());
        if(StringUtils.isEmpty(fallbackMethodValue))
            return proceed(joinPoint, methodName, retry, returnType);

        FallbackMethod fallbackMethod = FallbackMethod.create(fallbackMethodValue, method, joinPoint.getArgs(), joinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod, () -> proceed(joinPoint, methodName, retry, returnType)).apply();
    }

    private Object proceed(ProceedingJoinPoint joinPoint, String methodName,
                           com.github.liyibo1110.resilience4j.retry.Retry retry, Class<?> returnType) throws Throwable {
        if(CompletionStage.class.isAssignableFrom(returnType))
            return handleJoinPointCompletableFuture(joinPoint, retry);

        if(retryAspectExtList != null && !retryAspectExtList.isEmpty()) {
            for(RetryAspectExt retryAspectExt : retryAspectExtList) {
                if(retryAspectExt.canHandleReturnType(returnType))
                    return retryAspectExt.handle(joinPoint, retry, methodName);
            }
        }
        return handleDefaultJoinPoint(joinPoint, retry);
    }

    private com.github.liyibo1110.resilience4j.retry.Retry getOrCreateRetry(String methodName, String backend) {
        com.github.liyibo1110.resilience4j.retry.Retry retry = retryRegistry.retry(backend);
        if(logger.isDebugEnabled()) {
            logger.debug("Created or retrieved retry '{}' with max attempts rate '{}'  for method: '{}'",
                    backend, retry.getRetryConfig().getMaxAttempts(), methodName);
        }
        return retry;
    }

    @Nullable
    private Retry getRetryAnnotation(ProceedingJoinPoint joinPoint) {
        if(joinPoint.getTarget() instanceof Proxy) {
            logger.debug("The retry annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor.extractAnnotationFromProxy(joinPoint.getTarget(), Retry.class);
        }else {
            return AnnotationExtractor.extract(joinPoint.getTarget().getClass(), Retry.class);
        }
    }

    private Object handleDefaultJoinPoint(ProceedingJoinPoint joinPoint,
                                          com.github.liyibo1110.resilience4j.retry.Retry retry) throws Throwable {
        return retry.executeCheckedSupplier(joinPoint::proceed);
    }

    @SuppressWarnings("unchecked")
    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint joinPoint,
                                                    com.github.liyibo1110.resilience4j.retry.Retry retry) {
        return retry.executeCompletionStage(retryExecutorService, () -> {
            try {
                return (CompletionStage<Object>) joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }


    @Override
    public int getOrder() {
        return prop.getRetryAspectOrder();
    }

    @Override
    public void close() throws Exception {
        retryExecutorService.shutdown();
        try {
            if(!retryExecutorService.awaitTermination(5, TimeUnit.SECONDS))
                retryExecutorService.shutdownNow();
        } catch (InterruptedException e) {
            if(!retryExecutorService.isTerminated())
                retryExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
