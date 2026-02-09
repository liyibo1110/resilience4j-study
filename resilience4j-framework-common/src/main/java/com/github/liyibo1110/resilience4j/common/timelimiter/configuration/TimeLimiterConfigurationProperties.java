package com.github.liyibo1110.resilience4j.common.timelimiter.configuration;

import com.github.liyibo1110.resilience4j.common.CommonProperties;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.utils.ConfigUtils;
import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.StringUtils;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterConfig;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author liyibo
 * @date 2026-02-10 0:08
 */
public class TimeLimiterConfigurationProperties extends CommonProperties {
    private final Map<String, InstanceProperties> instances = new HashMap<>();
    private final Map<String, InstanceProperties> configs = new HashMap<>();

    public TimeLimiterConfig createTimeLimiterConfig(String limiter) {
        return createTimeLimiterConfig(limiter, getInstanceProperties(limiter), new CompositeCustomizer<>(Collections.emptyList()));
    }

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }

    @Nullable
    public InstanceProperties getInstanceProperties(String backend) {
        return instances.get(backend);
    }

    public TimeLimiterConfig createTimeLimiterConfig(String backendName, @Nullable InstanceProperties instanceProperties,
                                                     CompositeCustomizer<TimeLimiterConfigCustomizer> customizer) {
        if(instanceProperties == null)
            return TimeLimiterConfig.ofDefaults();

        if(StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {
            InstanceProperties baseProperties = configs.get(instanceProperties.getBaseConfig());
            if(baseProperties == null)
                throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
            return buildConfigFromBaseConfig(baseProperties, instanceProperties, customizer, backendName);
        }
        return buildTimeLimiterConfig(TimeLimiterConfig.custom(), instanceProperties, customizer, backendName);
    }

    private TimeLimiterConfig buildConfigFromBaseConfig(
            InstanceProperties baseProperties, InstanceProperties instanceProperties,
            CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer, String backendName) {

        ConfigUtils.mergePropertiesIfAny(baseProperties, instanceProperties);
        TimeLimiterConfig baseConfig = createTimeLimiterConfig(
                backendName, baseProperties, compositeTimeLimiterCustomizer);
        return buildTimeLimiterConfig(TimeLimiterConfig.from(baseConfig), instanceProperties,
                compositeTimeLimiterCustomizer, backendName);
    }

    private static TimeLimiterConfig buildTimeLimiterConfig(
            TimeLimiterConfig.Builder builder, @Nullable InstanceProperties instanceProperties,
            CompositeCustomizer<TimeLimiterConfigCustomizer> customizer, String backendName) {
        if(instanceProperties == null)
            return builder.build();

        if(instanceProperties.getTimeoutDuration() != null)
            builder.timeoutDuration(instanceProperties.getTimeoutDuration());

        if(instanceProperties.getCancelRunningFuture() != null)
            builder.cancelRunningFuture(instanceProperties.getCancelRunningFuture());

        customizer.getCustomizer(backendName).ifPresent(c -> c.customize(builder));
        return builder.build();
    }

    public static class InstanceProperties {

        private Duration timeoutDuration;
        private Boolean cancelRunningFuture;
        @Nullable
        private Integer eventConsumerBufferSize;

        @Nullable
        private String baseConfig;

        public Duration getTimeoutDuration() {
            return timeoutDuration;
        }

        public InstanceProperties setTimeoutDuration(Duration timeoutDuration) {
            Objects.requireNonNull(timeoutDuration);
            if(timeoutDuration.toMillis() < 0)
                throw new IllegalArgumentException("timeoutDuration must be greater than or equal to 0.");
            this.timeoutDuration = timeoutDuration;
            return this;
        }

        public Boolean getCancelRunningFuture() {
            return cancelRunningFuture;
        }

        public InstanceProperties setCancelRunningFuture(Boolean cancelRunningFuture) {
            this.cancelRunningFuture = cancelRunningFuture;
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

        @Nullable
        public String getBaseConfig() {
            return baseConfig;
        }

        public InstanceProperties setBaseConfig(@Nullable String baseConfig) {
            this.baseConfig = baseConfig;
            return this;
        }
    }
}
