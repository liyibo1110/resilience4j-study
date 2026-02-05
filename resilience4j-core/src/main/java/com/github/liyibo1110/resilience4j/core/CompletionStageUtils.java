package com.github.liyibo1110.resilience4j.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * CompletionState实例相关组合器工具（即把一次调用包装成链式组合）
 * @author liyibo
 * @date 2026-02-02 15:36
 */
public final class CompletionStageUtils {

    private CompletionStageUtils() {}

    /**
     * 先执行Supplier，然后调用handle方法，传入指定的BiFunction实例
     */
    public static <T, R> Supplier<CompletionStage<R>> andThen(Supplier<CompletionStage<T>> completionStageSupplier,
                                                              BiFunction<T, Throwable, R> handler) {
        return () -> completionStageSupplier.get().handle(handler);
    }

    /**
     * 给CompletionStage绑定exceptionHandler实例
     */
    public static <T> CompletionStage<T> recover(CompletionStage<T> completionStage, Function<Throwable, T> exceptionHandler) {
        return completionStage.exceptionally(exceptionHandler);
    }

    /**
     *
     */
    public static <T> CompletionStage<T> recover(CompletionStage<T> completionStage, List<Class<? extends Throwable>> exceptionTypes,
                                                 Function<Throwable, T> exceptionHandler) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        completionStage.whenComplete((result, t) -> {
           if(t != null) {
               // 如果是CompletionException或者ExecutionException，说明真正的异常被封装了在里面的，不要用外层的封装异常
               if(t instanceof CompletionException || t instanceof ExecutionException)
                   tryRecover(exceptionTypes, exceptionHandler, promise, t.getCause());
               else
                   tryRecover(exceptionTypes, exceptionHandler, promise, t);
           }else {
               promise.complete(result);
           }
        });
        return promise;
    }

    public static <X extends Throwable, T> CompletionStage<T> recover(CompletionStage<T> completionStage, Class<X> exceptionType,
                                                                      Function<Throwable, T> exceptionHandler) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        completionStage.whenComplete((result, t) -> {
           if(t != null) {
               if(t instanceof CompletionException || t instanceof ExecutionException)
                   tryRecover(exceptionType, exceptionHandler, promise, t.getCause());
               else
                   tryRecover(exceptionType, exceptionHandler, promise, t);
           }else {
               promise.complete(result);
           }
        });
        return promise;
    }

    /**
     * 如果给定的Throwable实例，存在于给定的异常列表里，则尝试调用响应的Function来恢复，否则通过传来的CompletableFuture直接返回异常
     */
    private static <T> void tryRecover(List<Class<? extends Throwable>> exceptionTypes, Function<Throwable, T> exceptionHandler,
                                       CompletableFuture<T> promise, Throwable throwable) {
        if(exceptionTypes.stream().anyMatch(et -> et.isAssignableFrom(throwable.getClass()))) {
            try {
                promise.complete(exceptionHandler.apply(throwable));
            } catch (Exception e) {
                promise.completeExceptionally(e);
            }
        }else {
            promise.completeExceptionally(throwable);
        }
    }

    private static <X extends Throwable, T> void tryRecover(Class<X> exceptionType, Function<Throwable, T> exceptionHandler,
                                       CompletableFuture<T> promise, Throwable throwable) {
        if(exceptionType.isAssignableFrom(throwable.getClass())) {
            try {
                promise.complete(exceptionHandler.apply(throwable));
            } catch (Exception e) {
                promise.completeExceptionally(e);
            }
        }else {
            promise.completeExceptionally(throwable);
        }
    }

    public static <T> Supplier<CompletionStage<T>> recover(Supplier<CompletionStage<T>> completionStageSupplier,
                                                           Function<Throwable, T> exceptionHandler) {
        return () -> recover(completionStageSupplier.get(), exceptionHandler);
    }

    public static <T, X extends Throwable> Supplier<CompletionStage<T>> recover(
            Supplier<CompletionStage<T>> completionStageSupplier, Class<X> exceptionType,
            Function<Throwable, T> exceptionHandler) {
        return () -> recover(completionStageSupplier.get(), exceptionType, exceptionHandler);
    }

    public static <T> Supplier<CompletionStage<T>> recover(
            Supplier<CompletionStage<T>> completionStageSupplier, List<Class<? extends Throwable>> exceptionTypes,
            Function<Throwable, T> exceptionHandler) {
        return () -> recover(completionStageSupplier.get(), exceptionTypes, exceptionHandler);
    }

    public static <T> CompletionStage<T> recover(CompletionStage<T> completionStage, Predicate<T> resultPredicate,
                                                UnaryOperator<T> resultHandler) {
        return completionStage.thenApply(result -> {
            if(resultPredicate.test(result))
                return resultHandler.apply(result);
            else
                return result;
        });
    }

    public static <T> Supplier<CompletionStage<T>> recover(Supplier<CompletionStage<T>> completionStageSupplier, Predicate<T> resultPredicate,
                                                        UnaryOperator<T> resultHandler) {
        return () -> recover(completionStageSupplier.get(), resultPredicate, resultHandler);
    }
}
