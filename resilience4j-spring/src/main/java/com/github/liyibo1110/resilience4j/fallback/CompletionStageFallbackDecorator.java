package com.github.liyibo1110.resilience4j.fallback;

import io.vavr.CheckedFunction0;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * 专门用在CompletionStage异步实例上面的FallbackDecorator实现（方法返回值是CompletionStage或其子类）
 * @author liyibo
 * @date 2026-02-12 23:31
 */
public class CompletionStageFallbackDecorator implements FallbackDecorator {
    @Override
    public boolean supports(Class<?> target) {
        return CompletionStage.class.isAssignableFrom(target);
    }

    @Override
    public CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod, CheckedFunction0<Object> supplier) {
        return supplier.andThen(request -> {
            CompletionStage<Object> completionStage = (CompletionStage)request;
            CompletableFuture promise = new CompletableFuture();
            completionStage.whenComplete((result, t) -> {
                if(t == null) {
                    promise.complete(result);
                }else {
                    if(t instanceof CompletionException || t instanceof ExecutionException)
                        this.tryRecover(fallbackMethod, promise, t.getCause());
                    else
                        this.tryRecover(fallbackMethod, promise, t);
                }
            });
            return promise;
        });
    }

    /**
     * 执行fallback方法，结果放到promise对象中
     */
    private void tryRecover(FallbackMethod fallbackMethod, CompletableFuture promise, Throwable t) {
        try {
            CompletionStage<Object> completionStage = (CompletionStage)fallbackMethod.fallback(t);
            completionStage.whenComplete((result, fallbackThrowable) -> {
                if(fallbackThrowable == null)
                    promise.complete(result);
                else
                    promise.completeExceptionally(fallbackThrowable);
            });
        } catch (Throwable fallbackThrowable) {
            promise.completeExceptionally(fallbackThrowable);
        }

    }
}
