package com.github.liyibo1110.resilience4j.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 检查classpath是否加载了Reactor
 * @author liyibo
 * @date 2026-02-11 13:42
 */
public class ReactorOnClasspathCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(ReactorOnClasspathCondition.class);
    private static final String CLASS_TO_CHECK = "reactor.core.publisher.Flux";
    private static final String R4J_REACTOR = "io.github.resilience4j.reactor.AbstractSubscriber";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return AspectUtil.checkClassIfFound(context, CLASS_TO_CHECK, (e) -> logger.debug("Reactor related Aspect extensions are not activated because Reactor is not on the classpath."))
                && AspectUtil.checkClassIfFound(context, R4J_REACTOR, (e) -> logger.debug("Reactor related Aspect extensions are not activated because Resilience4j Reactor module is not on the classpath."));
    }
}
