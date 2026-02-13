package com.github.liyibo1110.resilience4j.timelimiter.configure;

import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.FallbackMethod;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterConfig;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import com.github.liyibo1110.resilience4j.timelimiter.annotation.TimeLimiter;
import com.github.liyibo1110.resilience4j.utils.AnnotationExtractor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @date 2026-02-13 11:23
 */
public class TimeLimiterAspect implements Ordered, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TimeLimiterAspect.class);

    private final TimeLimiterRegistry timeLimiterRegistry;
    private final TimeLimiterConfigurationProperties prop;
    private final ScheduledExecutorService timeLimiterExecutorService;
    @Nullable
    private final List<TimeLimiterAspectExt> timeLimiterAspectExtList;
    private final FallbackDecorators fallbackDecorators;
    private final SpelResolver spelResolver;

    public TimeLimiterAspect(TimeLimiterRegistry timeLimiterRegistry,
                             TimeLimiterConfigurationProperties prop,
                             @Nullable List<TimeLimiterAspectExt> timeLimiterAspectExtList,
                             FallbackDecorators fallbackDecorators,
                             SpelResolver spelResolver,
                             @Nullable ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor) {
        this.timeLimiterRegistry = timeLimiterRegistry;
        this.prop = prop;
        this.timeLimiterAspectExtList = timeLimiterAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
        this.spelResolver = spelResolver;
        this.timeLimiterExecutorService = contextAwareScheduledThreadPoolExecutor != null
                ? contextAwareScheduledThreadPoolExecutor
                : Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Pointcut(value="@within(timeLimiter) || @annotation(timeLimiter)", argNames="timeLimiter")
    public void matchAnnotatedClassOrMethod(TimeLimiter timeLimiter) {}

    @Around(value = "matchAnnotatedClassOrMethod(timeLimiterAnnotation)", argNames="joinPoint, timeLimiterAnnotation")
    public Object timeLimiterAroundAdvice(ProceedingJoinPoint joinPoint, @Nullable TimeLimiter timeLimiterAnnotation) throws Throwable {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if(timeLimiterAnnotation == null)
            timeLimiterAnnotation = getTimeLimiterAnnotation(joinPoint);

        if(timeLimiterAnnotation == null)
            return joinPoint.proceed();

        String name = spelResolver.resolve(method, joinPoint.getArgs(), timeLimiterAnnotation.name());
        com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter timeLimiter = getOrCreateTimeLimiter(methodName, name);
        Class<?> returnType = method.getReturnType();

        String fallbackMethodValue = spelResolver.resolve(method, joinPoint.getArgs(), timeLimiterAnnotation.fallbackMethod());
        if(StringUtils.isEmpty(fallbackMethodValue))
            return proceed(joinPoint, methodName, timeLimiter, returnType);

        FallbackMethod fallbackMethod = FallbackMethod.create(fallbackMethodValue, method, joinPoint.getArgs(), joinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod, () -> proceed(joinPoint, methodName, timeLimiter, returnType)).apply();
    }

    private Object proceed(ProceedingJoinPoint joinPoint, String methodName,
                           com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter timeLimiter, Class<?> returnType) throws Throwable {
        if(timeLimiterAspectExtList != null && !timeLimiterAspectExtList.isEmpty()) {
            for(TimeLimiterAspectExt timeLimiterAspectExt : timeLimiterAspectExtList) {
                if(timeLimiterAspectExt.canHandleReturnType(returnType))
                    return timeLimiterAspectExt.handle(joinPoint, timeLimiter, methodName);
            }
        }

        if(!CompletionStage.class.isAssignableFrom(returnType))
            throw new IllegalReturnTypeException(returnType, methodName, "CompletionStage expected.");

        return handleJoinPointCompletableFuture(joinPoint, timeLimiter);
    }

    private com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter getOrCreateTimeLimiter(String methodName, String name) {
        com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(name);
        if (logger.isDebugEnabled()) {
            TimeLimiterConfig timeLimiterConfig = timeLimiter.getTimeLimiterConfig();
            logger.debug("Created or retrieved time limiter '{}' with timeout duration '{}' and cancelRunningFuture '{}' for method: '{}'",
                    name, timeLimiterConfig.getTimeoutDuration(), timeLimiterConfig.shouldCancelRunningFuture(), methodName
            );
        }
        return timeLimiter;
    }

    @Nullable
    private static TimeLimiter getTimeLimiterAnnotation(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getTarget() instanceof Proxy) {
            logger.debug("The TimeLimiter annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor.extractAnnotationFromProxy(joinPoint.getTarget(), TimeLimiter.class);
        } else {
            return AnnotationExtractor.extract(joinPoint.getTarget().getClass(), TimeLimiter.class);
        }
    }

    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint joinPoint,
                                                    com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter timeLimiter) throws Throwable {
        return timeLimiter.executeCompletionStage(timeLimiterExecutorService, () -> {
            try {
                return (CompletionStage<?>) joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }

    @Override
    public int getOrder() {
        return prop.getTimeLimiterAspectOrder();
    }

    @Override
    public void close() throws Exception {
        timeLimiterExecutorService.shutdown();
        try {
            if(!timeLimiterExecutorService.awaitTermination(5, TimeUnit.SECONDS))
                timeLimiterExecutorService.shutdownNow();
        } catch (InterruptedException e) {
            if(!timeLimiterExecutorService.isTerminated())
                timeLimiterExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
