package com.github.liyibo1110.resilience4j.spelresolver;

import java.lang.reflect.Method;

/**
 * @author liyibo
 * @date 2026-02-13 00:09
 */
public interface SpelResolver {

    /**
     * 解析特定Method上面的SPEL表达式
     */
    String resolve(Method method, Object[] arguments, String spelExpression);
}
