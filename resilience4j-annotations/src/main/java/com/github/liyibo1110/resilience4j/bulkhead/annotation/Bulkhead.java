package com.github.liyibo1110.resilience4j.bulkhead.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liyibo
 * @date 2026-02-10 15:08
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Bulkhead {
    String name();
    String fallbackMethod() default "";

    Type type() default Type.SEMAPHORE;

    enum Type {
        SEMAPHORE, THREADPOOL
    }
}
