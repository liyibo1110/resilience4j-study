package com.github.liyibo1110.resilience4j.feign;

import io.vavr.CheckedFunction1;

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * Fallback组件相关处理，类型T是Fallback对象（注意指的是Feign里面的Fallback组件，不是Resilience4j的）
 * @author liyibo
 * @date 2026-02-14 18:35
 */
interface FallbackHandler<T> {

    /**
     * 装饰invocationCall
     */
    CheckedFunction1<Object[], Object> decorate(CheckedFunction1<Object[], Object> invocationCall,
                                                Method method, Predicate<Exception> filter);

    /**
     * 验证给定的Method，是否为特定的Fallback实例（说白了就是验证传入的Fallback类型，必须是实现类，不能是原来的业务接口，比如UserService）
     */
    default void validateFallback(T fallback, Method method) {
        if(fallback.getClass().isAssignableFrom(method.getDeclaringClass()))
            throw new IllegalArgumentException("Cannot use the fallback [" + fallback.getClass() + "] for [" + method.getDeclaringClass() + "]!");
    }

    /**
     * 在Fallback实例中，寻找和Method一样的签名方法（就是找最终的fallback特定方法）
     */
    default Method getFallbackMethod(T fallbackInstance, Method method) {
        Method fallbackMethod;
        try {
            fallbackMethod = fallbackInstance.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Cannot use the fallback [" + fallbackInstance.getClass() + "] for [" + method.getDeclaringClass() + "]", e);
        }
        return fallbackMethod;
    }
}
