package com.github.liyibo1110.resilience4j.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Callable实例相关组合器工具（即把一次调用包装成链式组合）
 * @author liyibo
 * @date 2026-02-02 14:50
 */
public final class CallableUtils {

    private CallableUtils() {}

    /**
     * 执行callable之后，将返回值再传入指定的Function，最终返回Function的结果（类型依然是Callable）
     */
    public static <T, R> Callable<R> andThen(Callable<T> callable, Function<T, R> resultHandler) {
        return () -> resultHandler.apply(callable.call());
    }

    public static <T, R> Callable<R> andThen(Callable<T> callable, BiFunction<T, Throwable, R> handler) {
        return () -> {
            try {
                T result = callable.call();
                return handler.apply(result, null);
            } catch (Exception e) {
                return handler.apply(null, e);
            }
        };
    }

    public static <T, R> Callable<R> andThen(Callable<T> callable, Function<T, R> resultHandler,
                                             Function<Throwable, R> exceptionHandler) {
        return () -> {
            try {
                T result = callable.call();
                return resultHandler.apply(result);
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }

    /**
     * 执行callable之后，如果出现异常，则将异常传入指定的Function来尝试做恢复处理。
     */
    public static <T> Callable<T> recover(Callable<T> callable, Function<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }

    /**
     * 执行callable之后，如果结果通过Predicate检测，则将异常传入指定的Function来尝试做恢复处理。
     * UnaryOperator就是一个特殊的Function，限制了输入和输出必须是同一个类型
     */
    public static <T> Callable<T> recover(Callable<T> callable, Predicate<T> resultPredicate,
                                          UnaryOperator<T> resultHandler) {
        return () -> {
            T result = callable.call();
            if(resultPredicate.test(result))
                return resultHandler.apply(result);
            return result;
        };
    }

    /**
     * 执行callable之后，如果发生了异常，则按照给定的异常 -> Function来进行匹配执行
     */
    public static <T> Callable<T> recover(Callable<T> callable, List<Class<? extends Throwable>> exceptionTypes,
                                          Function<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                if(exceptionTypes.stream().anyMatch(et -> et.isAssignableFrom(e.getClass())))
                    return exceptionHandler.apply(e);
                else
                    throw e;
            }
        };
    }

    /**
     * 执行callable之后，如果发生了异常，并且符合给定的Function类型，则尝试处理
     */
    public static <X extends Throwable, T> Callable<T> recover(Callable<T> callable, Class<X> exceptionType,
                                                               Function<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                if(exceptionType.isAssignableFrom(e.getClass()))
                    return exceptionHandler.apply(e);
                else
                    throw e;
            }
        };
    }
}
