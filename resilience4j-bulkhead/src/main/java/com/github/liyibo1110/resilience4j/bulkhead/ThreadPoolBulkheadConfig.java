package com.github.liyibo1110.resilience4j.bulkhead;

import com.github.liyibo1110.resilience4j.core.ClassUtils;
import com.github.liyibo1110.resilience4j.core.ContextPropagator;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * FixedThreadPoolBulkhead实现专用的Config
 * @author liyibo
 * @date 2026-02-08 20:32
 */
public class ThreadPoolBulkheadConfig {
    public static final int DEFAULT_QUEUE_CAPACITY = 100;
    public static final Duration DEFAULT_KEEP_ALIVE_DURATION = Duration.ofMillis(20);
    public static final int DEFAULT_CORE_THREAD_POOL_SIZE =
            Runtime.getRuntime().availableProcessors() > 1
                    ? Runtime.getRuntime().availableProcessors() - 1
                    : 1;
    public static final int DEFAULT_MAX_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    public static final boolean DEFAULT_WRITABLE_STACK_TRACE_ENABLED = true;

    /** 最大线程池的个数 */
    private int maxThreadPoolSize = DEFAULT_MAX_THREAD_POOL_SIZE;
    private int coreThreadPoolSize = DEFAULT_CORE_THREAD_POOL_SIZE;
    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
    private Duration keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;
    private boolean writableStackTraceEnabled = DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
    private List<ContextPropagator> contextPropagators = new ArrayList<>();

    // 队列满了，对应的处理策略
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    private ThreadPoolBulkheadConfig() {}

    public static Builder custom() {
        return new Builder();
    }

    public static Builder from(ThreadPoolBulkheadConfig threadPoolBulkheadConfig) {
        return new Builder(threadPoolBulkheadConfig);
    }

    public static ThreadPoolBulkheadConfig ofDefaults() {
        return new Builder().build();
    }

    public Duration getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public int getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public int getCoreThreadPoolSize() {
        return coreThreadPoolSize;
    }

    public boolean isWritableStackTraceEnabled() {
        return writableStackTraceEnabled;
    }

    public List<ContextPropagator> getContextPropagator() {
        return contextPropagators;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadPoolBulkheadConfig{");
        sb.append("maxThreadPoolSize=").append(maxThreadPoolSize);
        sb.append(", coreThreadPoolSize=").append(coreThreadPoolSize);
        sb.append(", queueCapacity=").append(queueCapacity);
        sb.append(", keepAliveDuration=").append(keepAliveDuration);
        sb.append(", writableStackTraceEnabled=").append(writableStackTraceEnabled);
        sb.append(", contextPropagators=").append(contextPropagators);
        sb.append(", rejectExecutionHandle=").append(rejectedExecutionHandler.getClass().getSimpleName());
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private Class<? extends ContextPropagator>[] contextPropagatorClasses = new Class[0];
        private List<? extends ContextPropagator> contextPropagators = new ArrayList<>();
        private ThreadPoolBulkheadConfig config;

        public Builder(ThreadPoolBulkheadConfig bulkheadConfig) {
            this.config = bulkheadConfig;
        }

        public Builder() {
            config = new ThreadPoolBulkheadConfig();
        }

        public Builder maxThreadPoolSize(int maxThreadPoolSize) {
            if(maxThreadPoolSize < 1)
                throw new IllegalArgumentException("maxThreadPoolSize must be a positive integer value >= 1");
            config.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public Builder coreThreadPoolSize(int coreThreadPoolSize) {
            if(coreThreadPoolSize < 1)
                throw new IllegalArgumentException("coreThreadPoolSize must be a positive integer value >= 1");
            config.coreThreadPoolSize = coreThreadPoolSize;
            return this;
        }

        public final Builder contextPropagator(@Nullable Class<? extends ContextPropagator>... contextPropagatorClasses) {
            this.contextPropagatorClasses = contextPropagatorClasses != null
                    ? contextPropagatorClasses
                    : new Class[0];
            return this;
        }

        public final Builder contextPropagator(ContextPropagator... contextPropagators) {
            this.contextPropagators = contextPropagators != null
                    ? Arrays.stream(contextPropagators).collect(Collectors.toList())
                    : new ArrayList<>();
            return this;
        }

        public Builder queueCapacity(int queueCapacity) {
            if(queueCapacity < 1)
                throw new IllegalArgumentException("queueCapacity must be a positive integer value >= 1");
            config.queueCapacity = queueCapacity;
            return this;
        }

        public Builder keepAliveDuration(Duration keepAliveDuration) {
            if(keepAliveDuration.toMillis() < 0)
                throw new IllegalArgumentException("keepAliveDuration must be a positive integer value >= 0");
            config.keepAliveDuration = keepAliveDuration;
            return this;
        }

        public Builder writableStackTraceEnabled(boolean writableStackTraceEnabled) {
            config.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        public Builder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
            Objects.requireNonNull(rejectedExecutionHandler);
            config.rejectedExecutionHandler = rejectedExecutionHandler;
            return this;
        }

        public ThreadPoolBulkheadConfig build() {
            if(config.maxThreadPoolSize < config.coreThreadPoolSize)
                throw new IllegalArgumentException("maxThreadPoolSize must be a greater than or equals to coreThreadPoolSize");
            if(contextPropagatorClasses.length > 0) {
                config.contextPropagators.addAll(Arrays.stream(contextPropagatorClasses)
                        .map(ClassUtils::instantiateClassDefConstructor)
                        .collect(Collectors.toList()));
            }
            if(!contextPropagators.isEmpty())
                config.contextPropagators.addAll(this.contextPropagators);
            return config;
        }
    }
}
