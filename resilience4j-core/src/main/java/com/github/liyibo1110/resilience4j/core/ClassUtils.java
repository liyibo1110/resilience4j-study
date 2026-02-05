package com.github.liyibo1110.resilience4j.core;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author liyibo
 * @date 2026-02-02 16:47
 */
public final class ClassUtils {
    private static final String INSTANTIATION_ERROR_PREFIX = "Unable to create instance of class: ";

    private ClassUtils() {}

    /**
     * 生成IntervalBiFunction实例
     */
    public static <T> IntervalBiFunction<T> instantiateIntervalBiFunctionClass(Class<? extends IntervalBiFunction<T>> clazz) {
        try {
            Constructor<? extends IntervalBiFunction<T>> c = clazz.getConstructor();
            if(c != null)
                return c.newInstance();
            else
                throw new InstantiationException(INSTANTIATION_ERROR_PREFIX + clazz.getName());
        } catch (Exception e) {
            throw new InstantiationException(INSTANTIATION_ERROR_PREFIX + clazz.getName(), e);
        }
    }

    /**
     * 生成Predicate实例
     */
    public static <T> Predicate<T> instantiatePredicateClass(Class<? extends Predicate<T>> clazz) {
        try {
            Constructor<? extends Predicate<T>> c = clazz.getConstructor();
            if(c != null)
                return c.newInstance();
            else
                throw new InstantiationException(INSTANTIATION_ERROR_PREFIX + clazz.getName());
        } catch (Exception e) {
            throw new InstantiationException(INSTANTIATION_ERROR_PREFIX + clazz.getName(), e);
        }
    }

    /**
     * 生成特定class对应的对象实例（必须有无参数构造方法）
     */
    public static <T> T instantiateClassDefConstructor(Class<T> clazz) {
        Objects.requireNonNull(clazz, "class to instantiate should not be null");
        if(clazz.getConstructors().length > 0
            && Arrays.stream(clazz.getConstructors()).noneMatch(c -> c.getParameterCount() == 0)) {
            throw new InstantiationException("Default constructor is required to create instance of public class: " + clazz.getName());
        }
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InstantiationException(INSTANTIATION_ERROR_PREFIX + clazz.getName(), e);
        }
    }
}
