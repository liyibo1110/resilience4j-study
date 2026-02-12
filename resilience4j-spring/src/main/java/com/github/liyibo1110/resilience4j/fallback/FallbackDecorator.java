package com.github.liyibo1110.resilience4j.fallback;

import io.vavr.CheckedFunction0;

/**
 * fallback功能的装饰器
 * @author liyibo
 * @date 2026-02-11 15:46
 */
public interface FallbackDecorator {

    boolean supports(Class<?> target);

    CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod, CheckedFunction0<Object> supplier);
}

