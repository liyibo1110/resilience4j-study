package com.github.liyibo1110.resilience4j.feign;

import feign.InvocationHandlerFactory;
import feign.Target;
import io.vavr.CheckedFunction1;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author liyibo
 * @date 2026-02-14 19:44
 */
class FallbackDecorator<T> implements FeignDecorator {
    private final FallbackHandler<T> fallback;
    private final Predicate<Exception> filter;

    FallbackDecorator(FallbackHandler<T> fallback) {
        this(fallback, e -> true);
    }

    FallbackDecorator(FallbackHandler<T> fallback, Class<? extends Exception> filter) {
        this(fallback, filter::isInstance);
        Objects.requireNonNull(filter, "Filter cannot be null!");
    }

    FallbackDecorator(FallbackHandler<T> fallback, Predicate<Exception> filter) {
        this.fallback = Objects.requireNonNull(fallback, "Fallback cannot be null!");
        this.filter = Objects.requireNonNull(filter, "Filter cannot be null!");
    }

    @Override
    public CheckedFunction1<Object[], Object> decorate(CheckedFunction1<Object[], Object> invocationCall, Method method,
                                                       InvocationHandlerFactory.MethodHandler methodHandler, Target<?> target) {
        return fallback.decorate(invocationCall, method, filter);
    }
}
