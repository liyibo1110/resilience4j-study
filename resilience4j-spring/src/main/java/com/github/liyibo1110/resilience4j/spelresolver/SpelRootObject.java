package com.github.liyibo1110.resilience4j.spelresolver;

import java.lang.reflect.Method;

/**
 * @author liyibo
 * @date 2026-02-13 00:10
 */
public class SpelRootObject {
    private final String methodName;
    private final Object[] args;

    public SpelRootObject(Method method, Object[] args) {
        this.methodName = method.getName();
        this.args = args;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }
}
