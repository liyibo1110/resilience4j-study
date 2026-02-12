package com.github.liyibo1110.resilience4j.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 检查classpath是否加载了RxJava
 * @author liyibo
 * @date 2026-02-11 13:43
 */
public class RxJava2OnClasspathCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(RxJava2OnClasspathCondition.class);
    private static final String CLASS_TO_CHECK = "io.reactivex.Flowable";
    private static final String R4J_RXJAVA = "io.github.resilience4j.AbstractSubscriber";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return AspectUtil.checkClassIfFound(context, CLASS_TO_CHECK, (e) -> logger.debug("RxJava2 related Aspect extensions are not activated, because RxJava2 is not on the classpath."))
                && AspectUtil.checkClassIfFound(context, R4J_RXJAVA, (e) -> logger.debug("RxJava2 related Aspect extensions are not activated because Resilience4j RxJava2 module is not on the classpath."));
    }
}
