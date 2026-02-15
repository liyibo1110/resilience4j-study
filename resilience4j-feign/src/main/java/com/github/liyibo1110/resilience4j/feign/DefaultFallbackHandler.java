package com.github.liyibo1110.resilience4j.feign;

import io.vavr.CheckedFunction1;

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * 负责兜底的FallbackHandler实现，一开始就会被创建出来
 * @author liyibo
 * @date 2026-02-14 19:41
 */
class DefaultFallbackHandler<T> implements FallbackHandler<T> {
    private final T fallback;

    DefaultFallbackHandler(T fallback) {
        this.fallback = fallback;
    }

    @Override
    public CheckedFunction1<Object[], Object> decorate(CheckedFunction1<Object[], Object> invocationCall, Method method,
                                                       Predicate<Exception> filter) {
        validateFallback(fallback, method);
        Method fallbackMethod = getFallbackMethod(fallback, method);    // Feign原始对应的Fallback方法
        fallbackMethod.setAccessible(true);
        return args -> {
            try {
                return invocationCall.apply(args);
            } catch (Exception e) {
                if(filter.test(e))
                    return fallbackMethod.invoke(fallback, args);
                throw e;
            }
        };
    }
}
