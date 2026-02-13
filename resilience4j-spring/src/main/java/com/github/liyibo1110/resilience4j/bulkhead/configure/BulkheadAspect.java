package com.github.liyibo1110.resilience4j.bulkhead.configure;

import com.github.liyibo1110.resilience4j.bulkhead.BulkheadFullException;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.annotation.Bulkhead;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.FallbackMethod;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.utils.AnnotationExtractor;
import io.vavr.CheckedFunction0;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * @author liyibo
 * @date 2026-02-13 11:01
 */
@Aspect
public class BulkheadAspect implements Ordered {
    private static final Logger logger = LoggerFactory.getLogger(BulkheadAspect.class);

    private final BulkheadConfigurationProperties prop;
    private final BulkheadRegistry bulkheadRegistry;
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    private final @Nullable List<BulkheadAspectExt> bulkheadAspectExts;
    private final FallbackDecorators fallbackDecorators;
    private final SpelResolver spelResolver;

    public BulkheadAspect(BulkheadConfigurationProperties prop,
                          ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry, BulkheadRegistry bulkheadRegistry,
                          @Autowired(required=false) List<BulkheadAspectExt> bulkheadAspectExts,
                          FallbackDecorators fallbackDecorators,
                          SpelResolver spelResolver) {
        this.prop = prop;
        this.bulkheadRegistry = bulkheadRegistry;
        this.bulkheadAspectExts = bulkheadAspectExts;
        this.fallbackDecorators = fallbackDecorators;
        this.threadPoolBulkheadRegistry = threadPoolBulkheadRegistry;
        this.spelResolver = spelResolver;
    }

    @Pointcut(value="@within(Bulkhead) || @annotation(Bulkhead)", argNames="Bulkhead")
    public void matchAnnotatedClassOrMethod(Bulkhead Bulkhead) {}

    @Around(value="matchAnnotatedClassOrMethod(bulkheadAnnotation)", argNames="joinPoint, bulkheadAnnotation")
    public Object bulkheadAroundAdvice(ProceedingJoinPoint joinPoint,
                                       @Nullable Bulkhead bulkheadAnnotation) throws Throwable {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if(bulkheadAnnotation == null)
            bulkheadAnnotation = getBulkheadAnnotation(joinPoint);
        if(bulkheadAnnotation == null)
            return joinPoint.proceed();

        Class<?> returnType = method.getReturnType();
        String backend = spelResolver.resolve(method, joinPoint.getArgs(), bulkheadAnnotation.name());
        String fallbackMethodValue = spelResolver.resolve(method, joinPoint.getArgs(), bulkheadAnnotation.fallbackMethod());
        if (bulkheadAnnotation.type() == Bulkhead.Type.THREADPOOL) {
            if (StringUtils.isEmpty(fallbackMethodValue))
                return proceedInThreadPoolBulkhead(joinPoint, methodName, returnType, backend);

            return executeFallBack(joinPoint, fallbackMethodValue, method,
                    () -> proceedInThreadPoolBulkhead(joinPoint, methodName, returnType, backend));
        } else {
            com.github.liyibo1110.resilience4j.bulkhead.Bulkhead bulkhead = getOrCreateBulkhead(methodName, backend);
            if(StringUtils.isEmpty(fallbackMethodValue))
                return proceed(joinPoint, methodName, bulkhead, returnType);
            return executeFallBack(joinPoint, fallbackMethodValue, method,
                    () -> proceed(joinPoint, methodName, bulkhead, returnType));
        }

    }

    private Object executeFallBack(ProceedingJoinPoint joinPoint, String fallBackMethod,
                                   Method method, CheckedFunction0<Object> bulkhead) throws Throwable {
        FallbackMethod fallbackMethod = FallbackMethod.create(fallBackMethod, method, joinPoint.getArgs(), joinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod, bulkhead).apply();
    }

    private Object proceed(ProceedingJoinPoint joinPoint, String methodName,
                           com.github.liyibo1110.resilience4j.bulkhead.Bulkhead bulkhead, Class<?> returnType) throws Throwable {
        if (bulkheadAspectExts != null && !bulkheadAspectExts.isEmpty()) {
            for(BulkheadAspectExt bulkHeadAspectExt : bulkheadAspectExts) {
                if(bulkHeadAspectExt.canHandleReturnType(returnType))
                    return bulkHeadAspectExt.handle(joinPoint, bulkhead, methodName);
            }
        }
        if(CompletionStage.class.isAssignableFrom(returnType))
            return handleJoinPointCompletableFuture(joinPoint, bulkhead);
        return handleJoinPoint(joinPoint, bulkhead);
    }

    private com.github.liyibo1110.resilience4j.bulkhead.Bulkhead getOrCreateBulkhead(String methodName, String backend) {
        com.github.liyibo1110.resilience4j.bulkhead.Bulkhead bulkhead = bulkheadRegistry.bulkhead(backend);
        if (logger.isDebugEnabled()) {
            logger.debug("Created or retrieved bulkhead '{}' with max concurrent call '{}' and max wait time '{}ms' for method: '{}'",
                    backend, bulkhead.getBulkheadConfig().getMaxConcurrentCalls(),
                    bulkhead.getBulkheadConfig().getMaxWaitDuration().toMillis(), methodName);
        }
        return bulkhead;
    }

    @Nullable
    private Bulkhead getBulkheadAnnotation(ProceedingJoinPoint joinPoint) {
        if(logger.isDebugEnabled())
            logger.debug("bulkhead parameter is null");
        if(joinPoint.getTarget() instanceof Proxy) {
            logger.debug("The bulkhead annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor.extractAnnotationFromProxy(joinPoint.getTarget(), Bulkhead.class);
        }else {
            return AnnotationExtractor.extract(joinPoint.getTarget().getClass(), Bulkhead.class);
        }
    }

    private Object handleJoinPoint(ProceedingJoinPoint joinPoint,
                                   com.github.liyibo1110.resilience4j.bulkhead.Bulkhead bulkhead) throws Throwable {
        return bulkhead.executeCheckedSupplier(joinPoint::proceed);
    }

    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint joinPoint,
                                                    com.github.liyibo1110.resilience4j.bulkhead.Bulkhead bulkhead) {
        return bulkhead.executeCompletionStage(() -> {
            try {
                return (CompletionStage<?>) joinPoint.proceed();
            } catch (Throwable e) {
                throw new CompletionException(e);
            }
        });
    }

    private Object proceedInThreadPoolBulkhead(ProceedingJoinPoint joinPoint,
                                               String methodName, Class<?> returnType, String backend) throws Throwable {
        if(logger.isDebugEnabled())
            logger.debug("ThreadPool bulkhead invocation for method {} in backend {}", methodName, backend);

        ThreadPoolBulkhead threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(backend);
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            // threadPoolBulkhead.executeSupplier throws a BulkheadFullException, if the Bulkhead is full.
            // The RuntimeException is converted into an exceptionally completed future
            try {
                return threadPoolBulkhead.executeSupplier(() -> {
                    try {
                        return ((CompletionStage<?>) joinPoint.proceed()).toCompletableFuture().get();
                    } catch (ExecutionException e) {
                        throw new CompletionException(e.getCause());
                    } catch (Throwable e) {
                        throw new CompletionException(e);
                    }
                });
            } catch (BulkheadFullException ex){
                CompletableFuture<?> future = new CompletableFuture<>();
                future.completeExceptionally(ex);
                return future;
            }
        } else {
            throw new IllegalStateException("ThreadPool bulkhead is only applicable for completable futures ");
        }
    }

    @Override
    public int getOrder() {
        return prop.getBulkheadAspectOrder();
    }
}
