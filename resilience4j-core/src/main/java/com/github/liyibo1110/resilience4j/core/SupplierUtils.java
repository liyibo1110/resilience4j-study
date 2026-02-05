package com.github.liyibo1110.resilience4j.core;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Supplier实例相关组合器工具（即把一次调用包装成链式组合）
 * @author liyibo
 * @date 2026-02-02 15:28
 */
public final class SupplierUtils {

    private SupplierUtils() {}

    /**
     * 将Supplier的输出，再传入Function，最终还是返回Supplier
     */
    public static <T, R> Supplier<R> andThen(Supplier<T> supplier, Function<T, R> resultHandler) {
        return () -> resultHandler.apply(supplier.get());
    }

    public static <T, R> Supplier<R> andThen(Supplier<T> supplier, BiFunction<T, Throwable, R> handler) {
        return () -> {
            try {
                T result = supplier.get();
                return handler.apply(result, null);
            } catch (Exception e) {
                return handler.apply(null, e);
            }
        };
    }

    public static <T, R> Supplier<R> andThen(Supplier<T> supplier, Function<T, R> resultHandler,
                                             Function<Throwable, R> exceptionHandler) {
        return () -> {
            try {
                T result = supplier.get();
                return resultHandler.apply(result);
            } catch (Exception exception) {
                return exceptionHandler.apply(exception);
            }
        };
    }

    /**
     * 执行supplier之后，如果结果通过Predicate检测，则将异常传入指定的Function来尝试做恢复处理。
     * UnaryOperator就是一个特殊的Function，限制了输入和输出必须是同一个类型
     */
    public static <T> Supplier<T> recover(Supplier<T> supplier, Predicate<T> resultPredicate,
                                          UnaryOperator<T> resultHandler) {
        return () -> {
            T result = supplier.get();
            if(resultPredicate.test(result))
                return resultHandler.apply(result);
            return result;
        };
    }

    public static <T> Supplier<T> recover(Supplier<T> supplier, Function<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }

    public static <T> Supplier<T> recover(Supplier<T> supplier, List<Class<? extends Throwable>> exceptionTypes,
                                          Function<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                if(exceptionTypes.stream().anyMatch(et -> et.isAssignableFrom(e.getClass())))
                    return exceptionHandler.apply(e);
                else
                    throw e;
            }
        };
    }

    public static <X extends Throwable, T> Supplier<T> recover(Supplier<T> supplier, Class<X> exceptionType,
                                                               Function<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                if(exceptionType.isAssignableFrom(e.getClass()))
                    return exceptionHandler.apply(e);
                else
                    throw e;
            }
        };
    }
}
