package com.github.liyibo1110.resilience4j.core;

/**
 * 利用反射构造实例，失败对应的异常。
 * 注意JDK本身有同名的异常类，但不是RuntimeException
 * @author liyibo
 * @date 2026-02-02 17:38
 */
public class InstantiationException extends RuntimeException {
    public InstantiationException(String message) {
        super(message);
    }

    public InstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
