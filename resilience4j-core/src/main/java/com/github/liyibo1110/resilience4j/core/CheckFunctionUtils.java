package com.github.liyibo1110.resilience4j.core;

import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;

import java.util.List;
import java.util.function.Predicate;

/**
 * CheckFunction实例相关组合器工具（即把一次调用包装成链式组合）
 * CheckFunction就是特殊的一种Function，增加了抛出受查异常的功能
 * @author liyibo
 * @date 2026-02-02 15:55
 */
public final class CheckFunctionUtils {

    private CheckFunctionUtils() {}

    /**
     * 将Function0的输出，再传入Function2，最终还是返回Function0
     */
    public static <T, R> CheckedFunction0<R> andThen(CheckedFunction0<T> func, CheckedFunction2<T, Throwable, R> handler) {
        return () -> {
            try {
                return handler.apply(func.apply(), null);
            } catch (Throwable t) {
                return handler.apply(null, t);
            }
        };
    }

    /**
     * 执行Function0之后，如果返回异常，则将异常传入指定的Function1来尝试做恢复处理。
     */
    public static <T> CheckedFunction0<T> recover(CheckedFunction0<T> func, CheckedFunction1<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return func.apply();
            } catch (Throwable t) {
                return exceptionHandler.apply(t);
            }
        };
    }

    public static <T> CheckedFunction0<T> recover(CheckedFunction0<T> func, Predicate<T> resultPredicate,
                                                  CheckedFunction1<T, T> resultHandler) {
        return () -> {
            T result = func.apply();
            if(resultPredicate.test(result))
                return resultHandler.apply(result);
            return result;
        };
    }

    public static <T> CheckedFunction0<T> recover(CheckedFunction0<T> func, List<Class<? extends Throwable>> exceptionTypes,
                                                  CheckedFunction1<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return func.apply();
            } catch (Exception e) {
                if(exceptionTypes.stream().anyMatch(et -> et.isAssignableFrom(e.getClass())))
                    return exceptionHandler.apply(e);
                else
                    throw e;
            }
        };
    }

    public static <X extends Throwable, T> CheckedFunction0<T> recover(CheckedFunction0<T> func, Class<X> exceptionType,
                                                                       CheckedFunction1<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return func.apply();
            } catch (Throwable t) {
                if(exceptionType.isAssignableFrom(t.getClass()))
                    return exceptionHandler.apply(t);
                else
                    throw t;
            }
        };
    }
}
