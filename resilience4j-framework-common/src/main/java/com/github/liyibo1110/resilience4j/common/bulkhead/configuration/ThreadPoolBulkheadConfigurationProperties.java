package com.github.liyibo1110.resilience4j.common.bulkhead.configuration;

import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import com.github.liyibo1110.resilience4j.common.CommonProperties;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.ContextPropagator;
import com.github.liyibo1110.resilience4j.core.StringUtils;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author liyibo
 * @date 2026-02-09 23:30
 */
public class ThreadPoolBulkheadConfigurationProperties extends CommonProperties {
    private Map<String, InstanceProperties> instances = new HashMap<>();
    private Map<String, InstanceProperties> configs = new HashMap<>();

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    public Map<String, InstanceProperties> getBackends() {
        return instances;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }

    @Nullable
    public InstanceProperties getBackendProperties(String backend) {
        return instances.get(backend);
    }

    public ThreadPoolBulkheadConfig createThreadPoolBulkheadConfig(String backend,
                                                                   CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> customizer) {
        return createThreadPoolBulkheadConfig(getBackendProperties(backend), customizer, backend);
    }

    public ThreadPoolBulkheadConfig createThreadPoolBulkheadConfig(InstanceProperties instanceProperties,
            CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> customizer, String instanceName) {
        if(instanceProperties != null && StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {
            InstanceProperties baseProperties = configs.get(instanceProperties.getBaseConfig());
            if(baseProperties == null)
                throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
            return buildThreadPoolConfigFromBaseConfig(baseProperties, instanceProperties, customizer, instanceName);
        }
        return buildThreadPoolBulkheadConfig(ThreadPoolBulkheadConfig.custom(), instanceProperties, customizer, instanceName);
    }

    private ThreadPoolBulkheadConfig buildThreadPoolConfigFromBaseConfig(
            InstanceProperties baseProperties, InstanceProperties instanceProperties,
            CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> customizer,
            String instanceName) {
        ThreadPoolBulkheadConfig baseConfig = createThreadPoolBulkheadConfig(baseProperties, customizer, instanceName);
        return buildThreadPoolBulkheadConfig(ThreadPoolBulkheadConfig.from(baseConfig), instanceProperties, customizer, instanceName);
    }

    public ThreadPoolBulkheadConfig buildThreadPoolBulkheadConfig(
            ThreadPoolBulkheadConfig.Builder builder, InstanceProperties properties,
            CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> customizer,
            String instanceName) {
        if(properties == null)
            return ThreadPoolBulkheadConfig.custom().build();

        if(properties.getQueueCapacity() > 0)
            builder.queueCapacity(properties.getQueueCapacity());

        if(properties.getCoreThreadPoolSize() > 0)
            builder.coreThreadPoolSize(properties.getCoreThreadPoolSize());

        if(properties.getMaxThreadPoolSize() > 0)
            builder.maxThreadPoolSize(properties.getMaxThreadPoolSize());

        if(properties.getKeepAliveDuration() != null)
            builder.keepAliveDuration(properties.getKeepAliveDuration());

        if(properties.getWritableStackTraceEnabled() != null)
            builder.writableStackTraceEnabled(properties.getWritableStackTraceEnabled());

        if(properties.getContextPropagators() != null)
            builder.contextPropagator(properties.getContextPropagators());

        customizer.getCustomizer(instanceName).ifPresent(c -> c.customize(builder));
        return builder.build();
    }

    public static class InstanceProperties {
        @Nullable
        private Integer eventConsumerBufferSize;

        @Nullable
        private String baseConfig;

        @Nullable
        private Boolean writableStackTraceEnabled;

        private int maxThreadPoolSize;
        private int coreThreadPoolSize;
        private int queueCapacity;
        private Duration keepAliveDuration;

        @Nullable
        private Class<? extends ContextPropagator>[] contextPropagators;

        public int getMaxThreadPoolSize() {
            return maxThreadPoolSize;
        }

        public InstanceProperties setMaxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public int getCoreThreadPoolSize() {
            return coreThreadPoolSize;
        }

        public InstanceProperties setCoreThreadPoolSize(int coreThreadPoolSize) {
            this.coreThreadPoolSize = coreThreadPoolSize;
            return this;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public InstanceProperties setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
            return this;
        }


        public Duration getKeepAliveDuration() {
            return keepAliveDuration;
        }

        public InstanceProperties setKeepAliveDuration(Duration keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        public Integer getEventConsumerBufferSize() {
            return eventConsumerBufferSize;
        }

        public InstanceProperties setEventConsumerBufferSize(Integer eventConsumerBufferSize) {
            Objects.requireNonNull(eventConsumerBufferSize);
            if(eventConsumerBufferSize < 1)
                throw new IllegalArgumentException("eventConsumerBufferSize must be greater than or equal to 1.");
            this.eventConsumerBufferSize = eventConsumerBufferSize;
            return this;
        }

        public Boolean getWritableStackTraceEnabled() {
            return writableStackTraceEnabled;
        }

        public InstanceProperties setWritableStackTraceEnabled(boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        @Nullable
        public String getBaseConfig() {
            return baseConfig;
        }

        public InstanceProperties setBaseConfig(String baseConfig) {
            this.baseConfig = baseConfig;
            return this;
        }

        public Class<? extends ContextPropagator>[] getContextPropagators() {
            return contextPropagators;
        }

        @Deprecated
        public InstanceProperties setContextPropagator(Class<? extends ContextPropagator>... contextPropagators) {
            return setContextPropagators(contextPropagators);
        }

        public InstanceProperties setContextPropagators(Class<? extends ContextPropagator>... contextPropagators) {
            this.contextPropagators = contextPropagators;
            return this;
        }
    }
}
