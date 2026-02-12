package com.github.liyibo1110.resilience4j.fallback;

import com.github.liyibo1110.resilience4j.timelimiter.configure.IllegalReturnTypeException;
import io.vavr.CheckedFunction0;

/**
 * @author liyibo
 * @date 2026-02-12 17:26
 */
public class DefaultFallbackDecorator implements FallbackDecorator {
    @Override
    public boolean supports(Class<?> target) {
        return true;
    }

    @Override
    public CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod, CheckedFunction0<Object> supplier) {
        return () -> {
            try {
                return supplier.apply();
            } catch (IllegalReturnTypeException e) {
                throw e;
            } catch (Throwable t) {
                return fallbackMethod.fallback(t);
            }
        };
    }
}
