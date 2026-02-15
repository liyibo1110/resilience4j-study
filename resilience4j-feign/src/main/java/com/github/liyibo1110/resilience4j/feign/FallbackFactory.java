package com.github.liyibo1110.resilience4j.feign;

import io.vavr.CheckedFunction1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 负责按照不同异常动态创建Fallback的FallbackHandler实现，会在调用失败时才会被创建出来
 * @author liyibo
 * @date 2026-02-14 19:47
 */
class FallbackFactory<T> implements FallbackHandler<T> {
    private final Function<Exception, T> fallbackSupplier;

    FallbackFactory(Function<Exception, T> fallbackSupplier) {
        this.fallbackSupplier = fallbackSupplier;
    }

    @Override
    public CheckedFunction1<Object[], Object> decorate(CheckedFunction1<Object[], Object> invocationCall, Method method,
                                                       Predicate<Exception> filter) {
        return args -> {
            try {
                return invocationCall.apply(args);
            } catch (Exception e) {
                if(filter.test(e)) {
                    // 根据异常类型，动态创建Fallback实例
                    T fallbackInstance = fallbackSupplier.apply(e);
                    validateFallback(fallbackInstance, method);
                    Method fallbackMethod = getFallbackMethod(fallbackInstance, method);
                    try {
                        return fallbackMethod.invoke(fallbackInstance, args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                }
                throw e;
            }
        };
    }
}
