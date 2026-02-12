package com.github.liyibo1110.resilience4j.timelimiter.configure;

/**
 * @author liyibo
 * @date 2026-02-12 17:31
 */
public class IllegalReturnTypeException extends IllegalArgumentException {

    public IllegalReturnTypeException(Class<?> returnType, String methodName, String explanation) {
        super(String.join(" ", returnType.getName(), methodName,
                "has unsupported by @TimeLimiter return type.", explanation));
    }
}
