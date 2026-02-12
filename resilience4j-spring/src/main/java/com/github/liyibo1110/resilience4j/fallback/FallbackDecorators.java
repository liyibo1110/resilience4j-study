package com.github.liyibo1110.resilience4j.fallback;

import io.vavr.CheckedFunction0;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-11 15:45
 */
public class FallbackDecorators {
    /** 支持自定义的FallbackDecorator实现，没有合适的就会用默认的实现，注意这里是用方法返回值来查找的 */
    private final List<FallbackDecorator> fallbackDecorators;
    private final FallbackDecorator defaultFallbackDecorator = new DefaultFallbackDecorator();

    public FallbackDecorators(List<FallbackDecorator> fallbackDecorators) {
        this.fallbackDecorators = fallbackDecorators;
    }

    /**
     * 增加fallback功能装饰
     */
    public CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod, CheckedFunction0<Object> supplier) {
        return get(fallbackMethod.getReturnType()).decorate(fallbackMethod, supplier);
    }

    private FallbackDecorator get(Class<?> returnType) {
        return fallbackDecorators.stream().filter(d -> d.supports(returnType))
                .findFirst().orElse(defaultFallbackDecorator);
    }

    public List<FallbackDecorator> getFallbackDecorators() {
        return fallbackDecorators;
    }
}
