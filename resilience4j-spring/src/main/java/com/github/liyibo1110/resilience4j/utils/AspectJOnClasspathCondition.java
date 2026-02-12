package com.github.liyibo1110.resilience4j.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 检查classpath是否加载了AspectJ
 * @author liyibo
 * @date 2026-02-11 13:37
 */
public class AspectJOnClasspathCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(AspectJOnClasspathCondition.class);
    private static final String CLASS_TO_CHECK = "org.aspectj.lang.ProceedingJoinPoint";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return AspectUtil.checkClassIfFound(context, CLASS_TO_CHECK, (e) -> logger.debug("Aspects are not activated because AspectJ is not on the classpath."));
    }
}
