package com.github.liyibo1110.resilience4j.common.bulkhead.configuration;

import com.github.liyibo1110.resilience4j.bulkhead.BulkheadConfig;
import com.github.liyibo1110.resilience4j.common.CommonProperties;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.utils.ConfigUtils;
import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.StringUtils;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author liyibo
 * @date 2026-02-09 23:26
 */
public class BulkheadConfigurationProperties extends CommonProperties {
    private Map<String, InstanceProperties> instances = new HashMap<>();
    private Map<String, InstanceProperties> configs = new HashMap<>();

    public BulkheadConfig createBulkheadConfig(InstanceProperties instanceProperties,
                                               CompositeCustomizer<BulkheadConfigCustomizer> customizer,
                                               String instanceName) {
        if(StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {
            InstanceProperties baseProperties = configs.get(instanceProperties.getBaseConfig());
            if(baseProperties == null)
                throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
            return buildConfigFromBaseConfig(baseProperties, instanceProperties, customizer, instanceName);
        }
        return buildBulkheadConfig(BulkheadConfig.custom(), instanceProperties, customizer, instanceName);
    }

    private BulkheadConfig buildConfigFromBaseConfig(InstanceProperties baseProperties,
                                                     InstanceProperties instanceProperties,
                                                     CompositeCustomizer<BulkheadConfigCustomizer> customizer,
                                                     String instanceName) {
        ConfigUtils.mergePropertiesIfAny(baseProperties, instanceProperties);
        BulkheadConfig baseConfig = createBulkheadConfig(baseProperties, customizer, instanceName);
        return buildBulkheadConfig(BulkheadConfig.from(baseConfig), instanceProperties, customizer, instanceName);
    }

    private BulkheadConfig buildBulkheadConfig(BulkheadConfig.Builder builder,
                                               InstanceProperties instanceProperties,
                                               CompositeCustomizer<BulkheadConfigCustomizer> customizer,
                                               String instanceName) {
        if(instanceProperties.getMaxConcurrentCalls() != null)
            builder.maxConcurrentCalls(instanceProperties.getMaxConcurrentCalls());

        if(instanceProperties.getMaxWaitDuration() != null)
            builder.maxWaitDuration(instanceProperties.getMaxWaitDuration());

        if(instanceProperties.isWritableStackTraceEnabled() != null)
            builder.writableStackTraceEnabled(instanceProperties.isWritableStackTraceEnabled());

        customizer.getCustomizer(instanceName).ifPresent(c -> c.customize(builder));
        return builder.build();
    }

    @Nullable
    public InstanceProperties getBackendProperties(String backend) {
        return instances.get(backend);
    }

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    public Map<String, InstanceProperties> getBackends() {
        return instances;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }

    public static class InstanceProperties {
        private Integer maxConcurrentCalls;
        private Duration maxWaitDuration;
        private Boolean writableStackTraceEnabled;
        @Nullable
        private String baseConfig;
        @Nullable
        private Integer eventConsumerBufferSize;

        public Integer getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public InstanceProperties setMaxConcurrentCalls(Integer maxConcurrentCalls) {
            Objects.requireNonNull(maxConcurrentCalls);
            if(maxConcurrentCalls < 1)
                throw new IllegalArgumentException("maxConcurrentCalls must be greater than or equal to 1.");
            this.maxConcurrentCalls = maxConcurrentCalls;
            return this;
        }

        public Boolean isWritableStackTraceEnabled() {
            return writableStackTraceEnabled;
        }

        public InstanceProperties setWritableStackTraceEnabled(Boolean writableStackTraceEnabled) {
            Objects.requireNonNull(writableStackTraceEnabled);
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        public Duration getMaxWaitDuration() {
            return maxWaitDuration;
        }

        public InstanceProperties setMaxWaitDuration(Duration maxWaitDuration) {
            Objects.requireNonNull(maxWaitDuration);
            if(maxWaitDuration.toMillis() < 0)
                throw new IllegalArgumentException("maxWaitDuration must be greater than or equal to 0.");
            this.maxWaitDuration = maxWaitDuration;
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

        @Nullable
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
    }
}
