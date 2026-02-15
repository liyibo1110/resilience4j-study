package com.github.liyibo1110.resilience4j.feign;

import feign.InvocationHandlerFactory;
import feign.Target;
import feign.Util;
import io.vavr.CheckedFunction1;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liyibo
 * @date 2026-02-14 17:39
 */
class DecoratorInvocationHandler implements InvocationHandler {
    private final Target<?> target;
    // 用新结构替换了Feign的旧Map结构（value变成CheckedFunction1这玩意儿了，不然不能装饰）
    private final Map<Method, CheckedFunction1<Object[], Object>> decoratedDispatch;

    public DecoratorInvocationHandler(Target<?> target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch,
                                      FeignDecorator invocationDecorator) {
        this.target = Util.checkNotNull(target, "target");
        Util.checkNotNull(dispatch, "dispatch");
        this.decoratedDispatch = decorateMethodHandlers(dispatch, invocationDecorator, target);
    }

    /**
     * 将原始Map中的InvocationHandlerFactory.MethodHandler，转换成CheckedFunction1<Object[], Object>
     */
    private Map<Method, CheckedFunction1<Object[], Object>> decorateMethodHandlers(
            Map<Method, InvocationHandlerFactory.MethodHandler> dispatch,
            FeignDecorator invocationDecorator, Target<?> target) {
        final Map<Method, CheckedFunction1<Object[], Object>> map = new HashMap<>();
        for(final Map.Entry<Method, InvocationHandlerFactory.MethodHandler> entry : dispatch.entrySet()) {
            final Method method = entry.getKey();
            final InvocationHandlerFactory.MethodHandler methodHandler = entry.getValue();
            if(methodHandler != null) {
                CheckedFunction1<Object[], Object> decorated = invocationDecorator.decorate(methodHandler::invoke, method, methodHandler, target);
                map.put(method, decorated);
            }
        }
        return map;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return switch(method.getName()) {
            case "equals" -> equals(args.length > 0 ? args[0] : null);
            case "hashCode" -> hashCode();
            case "toString" -> toString();
            default -> decoratedDispatch.get(method).apply(args);   // apply就是启动整个套娃对象的启动点
        };
    }

    @Override
    public boolean equals(Object obj) {
        Object compareTo = obj;
        if(compareTo == null)
            return false;
        if(Proxy.isProxyClass(compareTo.getClass()))
            compareTo = Proxy.getInvocationHandler(compareTo);
        if(compareTo instanceof DecoratorInvocationHandler) {
            final DecoratorInvocationHandler other = (DecoratorInvocationHandler)compareTo;
            return target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
