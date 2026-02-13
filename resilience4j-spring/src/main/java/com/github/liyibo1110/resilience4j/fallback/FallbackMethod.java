package com.github.liyibo1110.resilience4j.fallback;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * fallback方法的调度器，负责：
 * 1、找到所有合法的fallback方法。
 * 2、建立异常 -> Method的映射关系。
 * 3、按照给定的异常，就近匹配到合适的fallback方法版本。
 * 要注意这个实例，是短生命周期组件，即每次被AOP拦截时才会产生，不是项目启动时就预先生成好一堆这种实例备用的，
 * 确切地说，是在AOP拦截过程中，每次都生成FallbackMethod实例（会走cache）。
 * @author liyibo
 * @date 2026-02-11 15:48
 */
public class FallbackMethod {
    /** cache */
    private static final Map<MethodMeta, Map<Class<?>, Method>> FALLBACK_METHODS_CACHE = new ConcurrentReferenceHashMap<>();

    /** 收集后的异常 -> fallback方法的映射，注意key是Throwable */
    private final Map<Class<?>, Method> fallbackMethods;

    /** 原始方法的参数（即@CircuitBreaker注解所标记的方法） */
    private final Object[] args;

    /** 原始方法的所在的类（即@CircuitBreaker注解所标记的方法） */
    private final Object target;

    /** 原始方法的返回类型（即@CircuitBreaker注解所标记的方法） */
    private final Class<?> returnType;

    private FallbackMethod(Map<Class<?>, Method> fallbackMethods, Class<?> originalMethodReturnType,
                           Object[] args, Object target) {
        this.fallbackMethods = fallbackMethods;
        this.args = args;
        this.target = target;
        this.returnType = originalMethodReturnType;
    }

    /**
     * FallbackMethod实例工厂。
     * 注意这里的fallbackMethodName，对应的是@CircuitBreaker注解上面的fallbackMethodName字段的值。
     */
    public static FallbackMethod create(String fallbackMethodName, Method originalMethod,
                                        Object[] args, Object target) throws NoSuchMethodException {
        MethodMeta methodMeta = new MethodMeta(fallbackMethodName, originalMethod.getParameterTypes(),
                originalMethod.getReturnType(), target.getClass());
        // 必须借助cache，因为这个extractMethods过程比较重
        Map<Class<?>, Method> methods = FALLBACK_METHODS_CACHE.computeIfAbsent(methodMeta, FallbackMethod::extractMethods);
        if(methods.isEmpty()) {
            throw new NoSuchMethodException(String.format("%s %s.%s(%s,%s)",
                    methodMeta.returnType, methodMeta.targetClass, methodMeta.fallbackMethodName,
                    StringUtils.arrayToDelimitedString(methodMeta.params, ","), Throwable.class));
        }else {
            return new FallbackMethod(methods, originalMethod.getReturnType(), args, target);
        }
    }


    /**
     * 提取实际的fallback方法
     */
    private static Map<Class<?>, Method> extractMethods(MethodMeta methodMeta) {
        Map<Class<?>, Method> methods = new HashMap<>();
        ReflectionUtils.doWithMethods(methodMeta.targetClass,
                method -> merge(method, methods),
                method -> filter(method, methodMeta));
        return methods;
    }

    /**
     * 将符合条件的（已经通过了下面filter的过滤逻辑了）Method，加入到Map中来
     */
    private static void merge(Method method, Map<Class<?>, Method> methods) {
        Class<?>[] fallbackParams = method.getParameterTypes();
        Class<?> exception = fallbackParams[fallbackParams.length - 1];
        Method similar = methods.get(exception);
        // 检查不能有相同版本的Method，有重复直接抛异常
        if(similar == null || Arrays.equals(similar.getParameterTypes(), method.getParameterTypes()))
            methods.put(exception, method);
        else
            throw new IllegalStateException("You have more that one fallback method that cover the same exception type " + exception.getName());
    }

    /**
     * 判断特定Method是不是fallback方法
     */
    private static boolean filter(Method method, MethodMeta methodMeta) {
        /* 1、方法名称要符合 */
        if(!method.getName().equals(methodMeta.fallbackMethodName))
            return false;
        /* 2、方法返回值类型要符合 */
        if(!methodMeta.returnType.isAssignableFrom(method.getReturnType()))
            return false;
        /* 3、如果方法参数只有1个，则必须是Throwable以及子类型 */
        if(method.getParameterCount() == 1)
            return Throwable.class.isAssignableFrom(method.getParameterTypes()[0]);
        /* 4、方法参数的数量要能对的上（fallback方法的参数数量 == 原始方法的参数数量 + 1） */
        if(method.getParameterCount() != methodMeta.params.length + 1)
            return false;
        /* 5、方法的每个参数，类型要对的上 */
        Class[] targetParams = method.getParameterTypes();
        for(int i = 0; i < methodMeta.params.length; i++) {
            if(methodMeta.params[i] != targetParams[i])
                return false;
        }
        /* 6、如果参数大于1个，fallback方法的最后一个参数，必须是Throwable以及子类型 */
        return Throwable.class.isAssignableFrom(targetParams[methodMeta.params.length]);
    }

    /**
     * 根据给定的异常类型，执行相应的fallback方法，并返回结果值，如果没匹配到fallback则继续抛异常
     */
    @Nullable
    public Object fallback(Throwable t) throws Throwable {
        // 就1个，则直接尝试执行
        if(fallbackMethods.size() == 1) {
            Map.Entry<Class<?>, Method> entry = fallbackMethods.entrySet().iterator().next();
            if(entry.getKey().isAssignableFrom(t.getClass()))
                return this.invoke(entry.getValue(), t);
            else
                throw t;
        }

        // 沿着继承关系向上找，直接找到符合自身的Method
        Method fallback = null;
        Class<?> thrownClass = t.getClass();
        while(fallback == null && thrownClass != Object.class) {
            fallback = fallbackMethods.get(thrownClass);
            thrownClass = thrownClass.getSuperclass();
        }

        if(fallback != null)
            return this.invoke(fallback, t);
        else
            throw t;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    private Object invoke(Method fallback, Throwable t) throws Throwable {
        boolean accessible = fallback.isAccessible();
        try {
            if(!accessible)
                ReflectionUtils.makeAccessible(fallback);
            if(args.length != 0) {
                // 替换参数，即要加上Throwable
                if(fallback.getParameterTypes().length == 1
                        && Throwable.class.isAssignableFrom(fallback.getParameterTypes()[0]))
                    return fallback.invoke(target, t);
                Object[] newArgs = Arrays.copyOf(args, args.length + 1);
                newArgs[args.length] = t;
                return fallback.invoke(target, newArgs);
            }else {
                return fallback.invoke(target, t);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } finally {
            if(!accessible)
                fallback.setAccessible(false);
        }

    }

    private static class MethodMeta {
        final String fallbackMethodName;
        final Class<?>[] params;
        final Class<?> returnType;
        final Class<?> targetClass;

        MethodMeta(String fallbackMethodName, Class<?>[] params, Class<?> returnType, Class<?> targetClass) {
            this.fallbackMethodName = fallbackMethodName;
            this.params = params;
            this.returnType = returnType;
            this.targetClass = targetClass;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null || getClass() != obj.getClass())
                return false;
            MethodMeta that = (MethodMeta)obj;
            return targetClass.equals(that.targetClass)
                    && fallbackMethodName.equals(that.fallbackMethodName)
                    && returnType.equals(that.returnType)
                    && Arrays.equals(params, that.params);
        }

        @Override
        public int hashCode() {
            return targetClass.getName().hashCode() ^ fallbackMethodName.hashCode();
        }
    }
}
