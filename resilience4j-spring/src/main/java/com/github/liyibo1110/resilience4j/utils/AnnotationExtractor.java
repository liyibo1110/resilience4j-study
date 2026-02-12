package com.github.liyibo1110.resilience4j.utils;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * @author liyibo
 * @date 2026-02-11 13:56
 */
public final class AnnotationExtractor {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationExtractor.class);

    private AnnotationExtractor() {}

    /**
     * 从指定的class提取注解
     */
    @Nullable
    public static <T extends Annotation> T extract(Class<?> targetClass, Class<T> annotationClass) {
        T anno = null;
        if(targetClass.isAnnotationPresent(annotationClass)) {  // isAnnotationPresent内部调用的是getAnnotation
            anno = targetClass.getAnnotation(annotationClass);  // 会去父类查注解，前提是注解必须有@Inherited，注意可能会是null（尽管通过了isAnnotationPresent检查）
            if(anno == null && logger.isDebugEnabled()) {
                logger.debug("TargetClass has no annotation '{}'", annotationClass.getSimpleName());
                anno = targetClass.getDeclaredAnnotation(annotationClass);  // 严格只查找本类，不查父类或者接口
                if(anno == null && logger.isDebugEnabled())
                    logger.debug("TargetClass has no declared annotation '{}'", annotationClass.getSimpleName());
            }
        }
        return anno;
    }

    /**
     * 从指定的proxy class提取注解（只支持JDK动态代理，CGLIB不行）
     */
    @Nullable
    public static <T extends Annotation> T extractAnnotationFromProxy(Object targetProxy, Class<T> annotationClass) {
        if(targetProxy.getClass().getInterfaces().length == 1)
            return extract(targetProxy.getClass().getInterfaces()[0], annotationClass); // 从接口里面找
        else if(targetProxy.getClass().getInterfaces().length > 1)
            return extractAnnotationFromClosestMatch(targetProxy, annotationClass);
        else
            return null;
    }

    /**
     * 从指定的proxy class提取注解（proxy实现了多个接口）
     */
    @Nullable
    private static <T extends Annotation> T extractAnnotationFromClosestMatch(Object targetProxy, Class<T> annotationClass) {
        int numberOfImplementations = targetProxy.getClass().getInterfaces().length;
        for(int depth = 0; depth < numberOfImplementations; depth++) {
            T anno = extract(targetProxy.getClass().getInterfaces()[depth], annotationClass);
            if(anno != null)
                return anno;
        }
        return null;
    }
}
