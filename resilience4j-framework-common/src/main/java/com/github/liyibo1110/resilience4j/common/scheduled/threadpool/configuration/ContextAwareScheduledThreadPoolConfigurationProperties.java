package com.github.liyibo1110.resilience4j.common.scheduled.threadpool.configuration;

import com.github.liyibo1110.resilience4j.core.ClassUtils;
import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import com.github.liyibo1110.resilience4j.core.ContextPropagator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-10 00:12
 */
public class ContextAwareScheduledThreadPoolConfigurationProperties {
    private int corePoolSize;
    private Class<? extends ContextPropagator>[] contextPropagators = new Class[0];

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        if(corePoolSize < 1)
            throw new IllegalArgumentException("corePoolSize must be a positive integer value >= 1");
        this.corePoolSize = corePoolSize;
    }

    public Class<? extends ContextPropagator>[] getContextPropagators() {
        return contextPropagators;
    }

    public void setContextPropagators(Class<? extends ContextPropagator>... contextPropagators) {
        this.contextPropagators = contextPropagators != null
                ? contextPropagators
                : new Class[0];
    }

    public ContextAwareScheduledThreadPoolExecutor build() {
        List<ContextPropagator> contextPropagatorsList = Arrays.stream(this.contextPropagators)
                .map(ClassUtils::instantiateClassDefConstructor)
                .collect(Collectors.toList());
        return ContextAwareScheduledThreadPoolExecutor.newScheduledThreadPool()
                .corePoolSize(this.corePoolSize)
                .contextPropagators(contextPropagatorsList.toArray(new ContextPropagator[0]))
                .build();
    }
}
