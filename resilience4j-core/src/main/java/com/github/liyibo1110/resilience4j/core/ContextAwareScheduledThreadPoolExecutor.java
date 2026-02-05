package com.github.liyibo1110.resilience4j.core;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 可调度线程池执行器的扩展实现，用来搭配ContextPropagator实现类和线程执行
 * @author liyibo
 * @date 2026-02-03 23:23
 */
public class ContextAwareScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private final List<ContextPropagator> contextPropagators;
    private static final String THREAD_PREFIX = "ContextAwareScheduledThreadPool";

    private ContextAwareScheduledThreadPoolExecutor(int corePoolSize,
                                                    @Nullable List<ContextPropagator> contextPropagators) {
        super(corePoolSize, new NamingThreadFactory(THREAD_PREFIX));
        this.contextPropagators = contextPropagators == null ? new ArrayList<>() : contextPropagators;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
        Map<String, String> mdcContextMap = this.getMdcContextMap();
        return super.schedule(ContextPropagator.decorateRunnable(this.contextPropagators, () -> {
            try {
                this.setMDCContext(mdcContextMap);
                r.run();
            } finally {
                MDC.clear();
            }
        }), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> c, long delay, TimeUnit unit) {
        Map<String, String> mdcContextMap = getMdcContextMap();
        return super.schedule(ContextPropagator.decorateCallable(this.contextPropagators, () -> {
            try {
                this.setMDCContext(mdcContextMap);
                return c.call();
            } finally {
                MDC.clear();
            }
        }), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initialDelay, long period, TimeUnit unit) {
        Map<String, String> mdcContextMap = getMdcContextMap();
        return super.scheduleAtFixedRate(ContextPropagator.decorateRunnable(this.contextPropagators, () -> {
            try {
                this.setMDCContext(mdcContextMap);
                r.run();
            } finally {
                MDC.clear();
            }
        }), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        Map<String, String> mdcContextMap = getMdcContextMap();
        return super.scheduleWithFixedDelay(ContextPropagator.decorateRunnable(this.contextPropagators, () -> {
            try {
                this.setMDCContext(mdcContextMap);
                r.run();
            } finally {
                MDC.clear();
            }
        }), initialDelay, delay, unit);
    }

    public List<ContextPropagator> getContextPropagators() {
        return Collections.unmodifiableList(this.contextPropagators);
    }

    private Map<String, String> getMdcContextMap() {
        return Optional.ofNullable(MDC.getCopyOfContextMap()).orElse(Collections.emptyMap());
    }

    /**
     * 所谓MDC（Mapped Diagnostic Context）即日志上下文字典，
     * 类似一个Map结构，例如MDC.put("traceId", "abc123");
     * 在pattern里面如果配置了[%X{traceId}]，就可以在实际log里输出abc123。
     * 至于为什么上面schedule单独设置了MDC，而不是放到contextPropagators里一起，只是因为MDC最常用。
     */
    private void setMDCContext(Map<String, String> contextMap) {
        MDC.clear();
        if(contextMap != null)
            MDC.setContextMap(contextMap);
    }

    public static Builder newScheduledThreadPool() {
        return new Builder();
    }

    public static class Builder {
        private List<ContextPropagator> contextPropagators = new ArrayList<>();
        private int corePoolSize;

        public Builder corePoolSize(int corePoolSize) {
            if(corePoolSize < 1)
                throw new IllegalArgumentException("corePoolSize must be a positive integer value >= 1");
            this.corePoolSize = corePoolSize;
            return this;
        }

        public Builder contextPropagators(ContextPropagator... contextPropagators) {
            this.contextPropagators = contextPropagators != null
                    ? Arrays.stream(contextPropagators).collect(Collectors.toList())
                    : new ArrayList<>();
            return this;
        }

        public ContextAwareScheduledThreadPoolExecutor build() {
            return new ContextAwareScheduledThreadPoolExecutor(corePoolSize, contextPropagators);
        }
    }
}
