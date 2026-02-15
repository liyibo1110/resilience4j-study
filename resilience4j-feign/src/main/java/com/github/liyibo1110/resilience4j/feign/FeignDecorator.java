package com.github.liyibo1110.resilience4j.feign;

import feign.InvocationHandlerFactory;
import feign.Target;
import io.vavr.CheckedFunction1;

import java.lang.reflect.Method;

/**
 * 对Feign的整体调用链进行装饰
 * @author liyibo
 * @date 2026-02-14 14:24
 */
@FunctionalInterface
public interface FeignDecorator {
    /**
     * 装饰invocationCall
     */
    CheckedFunction1<Object[], Object> decorate(CheckedFunction1<Object[], Object> invocationCall,
                                                Method method, InvocationHandlerFactory.MethodHandler methodHandler,
                                                Target<?> target);
}
