package com.github.liyibo1110.resilience4j.utils;

import org.springframework.context.annotation.ConditionContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Aspect相关工具
 * @author liyibo
 * @date 2026-02-11 13:38
 */
public final class AspectUtil {
    private AspectUtil() {}

    /**
     * 检查某个class是否存在于classpath
     */
    static boolean checkClassIfFound(ConditionContext context, String classToCheck, Consumer<Exception> exceptionConsumer) {
        try {
            Class<?> clazz = Objects.requireNonNull(context.getClassLoader(), "context must not be null").loadClass(classToCheck);
            return clazz != null;
        } catch (ClassNotFoundException e) {
            exceptionConsumer.accept(e);
            return false;
        }
    }

    public static <T> Set<T> newHashSet(T... objs) {
        Set<T> set = new HashSet<>();
        Collections.addAll(set, objs);
        return Collections.unmodifiableSet(set);
    }
}
