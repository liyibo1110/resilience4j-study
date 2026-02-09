package com.github.liyibo1110.resilience4j.common.ratelimiter.configuration;

import com.github.liyibo1110.resilience4j.common.CommonProperties;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.utils.ConfigUtils;
import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.StringUtils;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author liyibo
 * @date 2026-02-09 23:42
 */
public class RateLimiterConfigurationProperties extends CommonProperties {
    private Map<String, InstanceProperties> instances = new HashMap<>();
    private Map<String, InstanceProperties> configs = new HashMap<>();

    public Optional<InstanceProperties> findRateLimiterProperties(String name) {
        InstanceProperties instanceProperties = instances.get(name);
        if(instanceProperties == null)
            instanceProperties = configs.get("default");
        return Optional.ofNullable(instanceProperties);
    }

    public RateLimiterConfig createRateLimiterConfig(@Nullable InstanceProperties instanceProperties,
            CompositeCustomizer<RateLimiterConfigCustomizer> customizer, String instanceName) {
        if(instanceProperties == null)
            return RateLimiterConfig.ofDefaults();

        if(StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {
            InstanceProperties baseProperties = configs.get(instanceProperties.baseConfig);
            if(baseProperties == null)
                throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
            return buildConfigFromBaseConfig(baseProperties, instanceProperties, customizer, instanceName);
        }
        return buildRateLimiterConfig(RateLimiterConfig.custom(), instanceProperties, customizer, instanceName);
    }

    private RateLimiterConfig buildConfigFromBaseConfig(InstanceProperties baseProperties, InstanceProperties instanceProperties,
                                                        CompositeCustomizer<RateLimiterConfigCustomizer> customizer, String instanceName) {
        ConfigUtils.mergePropertiesIfAny(baseProperties, instanceProperties);
        RateLimiterConfig baseConfig = createRateLimiterConfig(baseProperties, customizer, instanceName);
        return buildRateLimiterConfig(RateLimiterConfig.from(baseConfig), instanceProperties, customizer, instanceName);
    }

    private RateLimiterConfig buildRateLimiterConfig(RateLimiterConfig.Builder builder, @Nullable InstanceProperties instanceProperties,
                                                     CompositeCustomizer<RateLimiterConfigCustomizer> customizer, String instanceName) {
        if(instanceProperties == null)
            return builder.build();

        if(instanceProperties.getLimitForPeriod() != null)
            builder.limitForPeriod(instanceProperties.getLimitForPeriod());

        if(instanceProperties.getLimitRefreshPeriod() != null)
            builder.limitRefreshPeriod(instanceProperties.getLimitRefreshPeriod());

        if(instanceProperties.getTimeoutDuration() != null)
            builder.timeoutDuration(instanceProperties.getTimeoutDuration());

        if(instanceProperties.getWritableStackTraceEnabled() != null)
            builder.writableStackTraceEnabled(instanceProperties.getWritableStackTraceEnabled());

        customizer.getCustomizer(instanceName).ifPresent(c -> c.customize(builder));
        return builder.build();
    }

    private InstanceProperties getLimiterProperties(String limiter) {
        return instances.get(limiter);
    }

    public RateLimiterConfig createRateLimiterConfig(String limiter,
                                                     CompositeCustomizer<RateLimiterConfigCustomizer> customizer) {
        return createRateLimiterConfig(getLimiterProperties(limiter), customizer, limiter);
    }

    @Nullable
    public InstanceProperties getInstanceProperties(String instance) {
        return instances.get(instance);
    }

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    public Map<String, InstanceProperties> getLimiters() {
        return instances;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }

    public static class InstanceProperties {

        private Integer limitForPeriod;
        private Duration limitRefreshPeriod;
        private Duration timeoutDuration;
        @Nullable
        private Boolean subscribeForEvents;
        @Nullable
        private Boolean allowHealthIndicatorToFail;
        @Nullable
        private Boolean registerHealthIndicator;
        @Nullable
        private Integer eventConsumerBufferSize;
        @Nullable
        private Boolean writableStackTraceEnabled;
        @Nullable
        private String baseConfig;

        @Nullable
        public Integer getLimitForPeriod() {
            return limitForPeriod;
        }

        public InstanceProperties setLimitForPeriod(Integer limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
            return this;
        }

        @Nullable
        public Duration getLimitRefreshPeriod() {
            return limitRefreshPeriod;
        }

        public InstanceProperties setLimitRefreshPeriod(Duration limitRefreshPeriod) {
            this.limitRefreshPeriod = limitRefreshPeriod;
            return this;
        }

        @Nullable
        public Duration getTimeoutDuration() {
            return timeoutDuration;
        }

        public InstanceProperties setTimeoutDuration(Duration timeout) {
            this.timeoutDuration = timeout;
            return this;
        }

        public Boolean getWritableStackTraceEnabled() {
            return this.writableStackTraceEnabled;
        }

        public InstanceProperties setWritableStackTraceEnabled(Boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        public Boolean getSubscribeForEvents() {
            return subscribeForEvents;
        }

        public InstanceProperties setSubscribeForEvents(Boolean subscribeForEvents) {
            this.subscribeForEvents = subscribeForEvents;
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
        public Boolean getAllowHealthIndicatorToFail() {
            return allowHealthIndicatorToFail;
        }

        public InstanceProperties setAllowHealthIndicatorToFail(Boolean allowHealthIndicatorToFail) {
            this.allowHealthIndicatorToFail = allowHealthIndicatorToFail;
            return this;
        }

        public Boolean getRegisterHealthIndicator() {
            return registerHealthIndicator;
        }

        public InstanceProperties setRegisterHealthIndicator(Boolean registerHealthIndicator) {
            this.registerHealthIndicator = registerHealthIndicator;
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
    }
}
