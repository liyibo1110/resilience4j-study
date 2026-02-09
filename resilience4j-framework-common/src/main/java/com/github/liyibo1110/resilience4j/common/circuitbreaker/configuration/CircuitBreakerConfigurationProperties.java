package com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.common.CommonProperties;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.utils.ConfigUtils;
import com.github.liyibo1110.resilience4j.core.ClassUtils;
import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.IntervalFunction;
import com.github.liyibo1110.resilience4j.core.StringUtils;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author liyibo
 * @date 2026-02-09 18:09
 */
public class CircuitBreakerConfigurationProperties extends CommonProperties {

    /** 具体的组件实例上面的配置（优先级高于模板配置） */
    private Map<String, InstanceProperties> instances = new HashMap<>();

    /** 对应SpringBoot的模板配置 */
    private Map<String, InstanceProperties> configs = new HashMap<>();

    public Optional<InstanceProperties> findCircuitBreakerProperties(String name) {
        InstanceProperties instanceProperties = instances.get(name);
        if(instanceProperties == null)
            instanceProperties = configs.get("default");
        return Optional.ofNullable(instanceProperties);
    }

    public CircuitBreakerConfig createCircuitBreakerConfig(String backendName, InstanceProperties instanceProperties,
                                                           CompositeCustomizer<CircuitBreakerConfigCustomizer> customizer) {
        if(StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {    // 有baseConfig，先尝试合并配置
            InstanceProperties baseProperties = configs.get(instanceProperties.getBaseConfig());
            if(baseProperties == null)
                throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
            return this.buildConfigFromBaseConfig(instanceProperties, baseProperties, customizer, backendName);
        }
        return this.buildConfig(CircuitBreakerConfig.custom(), instanceProperties, customizer, backendName);
    }

    private CircuitBreakerConfig buildConfigFromBaseConfig(InstanceProperties instanceProperties, InstanceProperties baseProperties,
                                                           CompositeCustomizer<CircuitBreakerConfigCustomizer> customizer,
                                                           String backendName) {
        // 合并实例配置和模板配置
        ConfigUtils.mergePropertiesIfAny(instanceProperties, baseProperties);
        CircuitBreakerConfig baseConfig = createCircuitBreakerConfig(backendName, baseProperties, customizer);
        return this.buildConfig(CircuitBreakerConfig.from(baseConfig), instanceProperties, customizer, backendName);
    }

    /**
     * 构建生成CircuitBreakerConfig实例
     */
    private CircuitBreakerConfig buildConfig(CircuitBreakerConfig.Builder builder, InstanceProperties properties,
                                             CompositeCustomizer<CircuitBreakerConfigCustomizer> customizer, String backendName) {
        if(properties == null)
            return builder.build();

        if(properties.enableExponentialBackoff != null && properties.enableExponentialBackoff
            && properties.enableRandomizedWait != null && properties.enableRandomizedWait) {
            throw new IllegalStateException("you can not enable Exponential backoff policy and randomized delay at the same time , please enable only one of them");
        }

        this.configureCircuitBreakerOpenStateIntervalFunction(properties, builder);

        if(properties.getFailureRateThreshold() != null)
            builder.failureRateThreshold(properties.getFailureRateThreshold());

        if(properties.getWritableStackTraceEnabled() != null)
            builder.writableStackTraceEnabled(properties.getWritableStackTraceEnabled());

        if(properties.getSlowCallRateThreshold() != null)
            builder.slowCallRateThreshold(properties.getSlowCallRateThreshold());

        if(properties.getSlowCallDurationThreshold() != null)
            builder.slowCallDurationThreshold(properties.getSlowCallDurationThreshold());

        if(properties.getMaxWaitDurationInHalfOpenState() != null)
            builder.maxWaitDurationInHalfOpenState(properties.getMaxWaitDurationInHalfOpenState());

        if(properties.getRingBufferSizeInClosedState() != null)
            builder.ringBufferSizeInClosedState(properties.getRingBufferSizeInClosedState());

        if(properties.getSlidingWindowSize() != null)
            builder.slidingWindowSize(properties.getSlidingWindowSize());

        if(properties.getMinimumNumberOfCalls() != null)
            builder.minimumNumberOfCalls(properties.getMinimumNumberOfCalls());

        if(properties.getSlidingWindowType() != null)
            builder.slidingWindowType(properties.getSlidingWindowType());

        if(properties.getRingBufferSizeInHalfOpenState() != null)
            builder.ringBufferSizeInHalfOpenState(properties.getRingBufferSizeInHalfOpenState());

        if(properties.getPermittedNumberOfCallsInHalfOpenState() != null)
            builder.permittedNumberOfCallsInHalfOpenState(properties.getPermittedNumberOfCallsInHalfOpenState());

        if (properties.recordExceptions != null) {
            builder.recordExceptions(properties.recordExceptions);
            // if instance config has set recordExceptions, then base config's recordExceptionPredicate is useless.
            builder.recordException(null);
        }

        if(properties.recordFailurePredicate != null)
            buildRecordFailurePredicate(properties, builder);

        if(properties.ignoreExceptions != null) {
            builder.ignoreExceptions(properties.ignoreExceptions);
            builder.ignoreException(null);
        }

        if(properties.automaticTransitionFromOpenToHalfOpenEnabled != null)
            builder.automaticTransitionFromOpenToHalfOpenEnabled(properties.automaticTransitionFromOpenToHalfOpenEnabled);

        // 自定义配置加载扩展点
        customizer.getCustomizer(backendName).ifPresent(c -> c.customize(builder));
        return builder.build();
    }

    private void configureCircuitBreakerOpenStateIntervalFunction(InstanceProperties properties, CircuitBreakerConfig.Builder builder) {
        if(properties.getWaitDurationInOpenState() != null && properties.getWaitDurationInOpenState().toMillis() > 0) {
            if(properties.getEnableExponentialBackoff() != null && properties.getEnableExponentialBackoff())
                configureEnableExponentialBackoff(properties, builder);
            else if(properties.getEnableRandomizedWait() != null && properties.getEnableRandomizedWait())
                configureEnableRandomizedWait(properties, builder);
            else
                builder.waitDurationInOpenState(properties.getWaitDurationInOpenState());
        }
    }

    private void configureEnableExponentialBackoff(InstanceProperties properties, CircuitBreakerConfig.Builder builder) {
        Duration maxWaitDuration = properties.getExponentialMaxWaitDurationInOpenState();
        Double backoffMultiplier = properties.getExponentialBackoffMultiplier();
        Duration waitDuration = properties.getWaitDurationInOpenState();
        if (maxWaitDuration != null && backoffMultiplier != null)
            builder.waitIntervalFunctionInOpenState(IntervalFunction.ofExponentialBackoff(waitDuration, backoffMultiplier, maxWaitDuration));
        else if (backoffMultiplier != null)
            builder.waitIntervalFunctionInOpenState(IntervalFunction.ofExponentialBackoff(waitDuration, backoffMultiplier));
        else
            builder.waitIntervalFunctionInOpenState(IntervalFunction.ofExponentialBackoff(waitDuration));
    }

    private void configureEnableRandomizedWait(InstanceProperties properties, CircuitBreakerConfig.Builder builder) {
        Duration waitDuration = properties.getWaitDurationInOpenState();
        if(properties.getRandomizedWaitFactor() != null)
            builder.waitIntervalFunctionInOpenState(IntervalFunction.ofRandomized(waitDuration, properties.getRandomizedWaitFactor()));
        else
            builder.waitIntervalFunctionInOpenState(IntervalFunction.ofRandomized(waitDuration));
    }

    private void buildRecordFailurePredicate(InstanceProperties properties, CircuitBreakerConfig.Builder builder) {
        if(properties.getRecordFailurePredicate() != null) {
            Predicate<Throwable> predicate = ClassUtils.instantiatePredicateClass(properties.getRecordFailurePredicate());
            if(predicate != null)
                builder.recordException(predicate);
        }
    }

    @Nullable
    public InstanceProperties getBackendProperties(String backend) {
        return instances.get(backend);
    }

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    /**
     * 为了向后兼容
     */
    public Map<String, InstanceProperties> getBackends() {
        return instances;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }

    public static class InstanceProperties {
        @Nullable
        private Duration waitDurationInOpenState;

        @Nullable
        private Duration slowCallDurationThreshold;

        @Nullable
        private Duration maxWaitDurationInHalfOpenState;

        @Nullable
        private Float failureRateThreshold;

        @Nullable
        private Float slowCallRateThreshold;

        @Nullable
        @Deprecated
        @SuppressWarnings("DeprecatedIsStillUsed") // Left for backward compatibility
        private Integer ringBufferSizeInClosedState;

        @Nullable
        private CircuitBreakerConfig.SlidingWindowType slidingWindowType;

        @Nullable
        private Integer slidingWindowSize;

        @Nullable
        private Integer minimumNumberOfCalls;

        @Nullable
        private Integer permittedNumberOfCallsInHalfOpenState;

        @Nullable
        @Deprecated
        @SuppressWarnings("DeprecatedIsStillUsed") // Left for backward compatibility
        private Integer ringBufferSizeInHalfOpenState;

        @Nullable
        private Boolean automaticTransitionFromOpenToHalfOpenEnabled;

        @Nullable
        private Boolean writableStackTraceEnabled;

        @Nullable
        private Boolean allowHealthIndicatorToFail;

        @Nullable
        private Integer eventConsumerBufferSize;

        @Nullable
        private Boolean registerHealthIndicator;

        @Nullable
        private Class<Predicate<Throwable>> recordFailurePredicate;

        @Nullable
        private Class<? extends Throwable>[] recordExceptions;

        @Nullable
        private Class<? extends Throwable>[] ignoreExceptions;

        @Nullable
        private String baseConfig;

        @Nullable
        private Boolean enableExponentialBackoff;

        private Double exponentialBackoffMultiplier;

        private Duration exponentialMaxWaitDurationInOpenState;

        @Nullable
        private Boolean enableRandomizedWait;

        private Double randomizedWaitFactor;

        @Nullable
        public Float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public InstanceProperties setFailureRateThreshold(Float failureRateThreshold) {
            Objects.requireNonNull(failureRateThreshold);
            if(failureRateThreshold < 1 || failureRateThreshold > 100)
                throw new IllegalArgumentException("failureRateThreshold must be between 1 and 100.");
            this.failureRateThreshold = failureRateThreshold;
            return this;
        }

        @Nullable
        public Duration getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        public InstanceProperties setWaitDurationInOpenState(Duration waitDurationInOpenStateMillis) {
            Objects.requireNonNull(waitDurationInOpenStateMillis);
            if(waitDurationInOpenStateMillis.toMillis() < 1)
                throw new IllegalArgumentException("waitDurationInOpenStateMillis must be greater than or equal to 1 millis.");
            this.waitDurationInOpenState = waitDurationInOpenStateMillis;
            return this;
        }

        @Nullable
        public Integer getRingBufferSizeInClosedState() {
            return ringBufferSizeInClosedState;
        }

        @Deprecated
        public InstanceProperties setRingBufferSizeInClosedState(Integer ringBufferSizeInClosedState) {
            Objects.requireNonNull(ringBufferSizeInClosedState);
            if(ringBufferSizeInClosedState < 1)
                throw new IllegalArgumentException("ringBufferSizeInClosedState must be greater than or equal to 1.");
            this.ringBufferSizeInClosedState = ringBufferSizeInClosedState;
            return this;
        }

        @Nullable
        public Integer getRingBufferSizeInHalfOpenState() {
            return ringBufferSizeInHalfOpenState;
        }

        @Deprecated
        public InstanceProperties setRingBufferSizeInHalfOpenState(Integer ringBufferSizeInHalfOpenState) {
            Objects.requireNonNull(ringBufferSizeInHalfOpenState);
            if(ringBufferSizeInHalfOpenState < 1)
                throw new IllegalArgumentException("ringBufferSizeInHalfOpenState must be greater than or equal to 1.");
            this.ringBufferSizeInHalfOpenState = ringBufferSizeInHalfOpenState;
            return this;
        }

        public Boolean getAutomaticTransitionFromOpenToHalfOpenEnabled() {
            return this.automaticTransitionFromOpenToHalfOpenEnabled;
        }

        public InstanceProperties setAutomaticTransitionFromOpenToHalfOpenEnabled(Boolean automaticTransitionFromOpenToHalfOpenEnabled) {
            this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
            return this;
        }

        @Nullable
        public Boolean getWritableStackTraceEnabled() {
            return this.writableStackTraceEnabled;
        }

        public InstanceProperties setWritableStackTraceEnabled(Boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
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

        @Nullable
        public Boolean getAllowHealthIndicatorToFail() {
            return allowHealthIndicatorToFail;
        }

        public InstanceProperties setAllowHealthIndicatorToFail(Boolean allowHealthIndicatorToFail) {
            this.allowHealthIndicatorToFail = allowHealthIndicatorToFail;
            return this;
        }

        @Nullable
        public Boolean getRegisterHealthIndicator() {
            return registerHealthIndicator;
        }

        public InstanceProperties setRegisterHealthIndicator(Boolean registerHealthIndicator) {
            this.registerHealthIndicator = registerHealthIndicator;
            return this;
        }

        @Nullable
        public Class<Predicate<Throwable>> getRecordFailurePredicate() {
            return recordFailurePredicate;
        }

        public InstanceProperties setRecordFailurePredicate(Class<Predicate<Throwable>> recordFailurePredicate) {
            this.recordFailurePredicate = recordFailurePredicate;
            return this;
        }

        @Nullable
        public Class<? extends Throwable>[] getRecordExceptions() {
            return recordExceptions;
        }

        public InstanceProperties setRecordExceptions(Class<? extends Throwable>[] recordExceptions) {
            this.recordExceptions = recordExceptions;
            return this;
        }

        @Nullable
        public Class<? extends Throwable>[] getIgnoreExceptions() {
            return ignoreExceptions;
        }

        public InstanceProperties setIgnoreExceptions(Class<? extends Throwable>[] ignoreExceptions) {
            this.ignoreExceptions = ignoreExceptions;
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
        public Integer getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public InstanceProperties setPermittedNumberOfCallsInHalfOpenState(Integer permittedNumberOfCallsInHalfOpenState) {
            Objects.requireNonNull(permittedNumberOfCallsInHalfOpenState);
            if(permittedNumberOfCallsInHalfOpenState < 1)
                throw new IllegalArgumentException("permittedNumberOfCallsInHalfOpenState must be greater than or equal to 1.");
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            return this;
        }

        @Nullable
        public Integer getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public InstanceProperties setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
            Objects.requireNonNull(minimumNumberOfCalls);
            if(minimumNumberOfCalls < 1)
                throw new IllegalArgumentException("minimumNumberOfCalls must be greater than or equal to 1.");
            this.minimumNumberOfCalls = minimumNumberOfCalls;
            return this;
        }

        @Nullable
        public Integer getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public InstanceProperties setSlidingWindowSize(Integer slidingWindowSize) {
            Objects.requireNonNull(slidingWindowSize);
            if(slidingWindowSize < 1)
                throw new IllegalArgumentException("slidingWindowSize must be greater than or equal to 1.");
            this.slidingWindowSize = slidingWindowSize;
            return this;
        }

        @Nullable
        public Float getSlowCallRateThreshold() {
            return slowCallRateThreshold;
        }

        public InstanceProperties setSlowCallRateThreshold(Float slowCallRateThreshold) {
            Objects.requireNonNull(slowCallRateThreshold);
            if(slowCallRateThreshold < 1 || slowCallRateThreshold > 100)
                throw new IllegalArgumentException("slowCallRateThreshold must be between 1 and 100.");
            this.slowCallRateThreshold = slowCallRateThreshold;
            return this;
        }

        @Nullable
        public Duration getSlowCallDurationThreshold() {
            return slowCallDurationThreshold;
        }

        @Nullable
        public Duration getMaxWaitDurationInHalfOpenState() {
            return maxWaitDurationInHalfOpenState;
        }

        public InstanceProperties setSlowCallDurationThreshold(Duration slowCallDurationThreshold) {
            Objects.requireNonNull(slowCallDurationThreshold);
            if(slowCallDurationThreshold.toNanos() < 1)
                throw new IllegalArgumentException("waitDurationInOpenStateMillis must be greater than or equal to 1 nanos.");
            this.slowCallDurationThreshold = slowCallDurationThreshold;
            return this;
        }

        public InstanceProperties setMaxWaitDurationInHalfOpenState(Duration maxWaitDurationInHalfOpenState) {
            Objects.requireNonNull(maxWaitDurationInHalfOpenState);
            if(maxWaitDurationInHalfOpenState.toMillis() < 1)
                throw new IllegalArgumentException("maxWaitDurationInHalfOpenState must be greater than or equal to 1 ms.");
            this.maxWaitDurationInHalfOpenState = maxWaitDurationInHalfOpenState;
            return this;
        }

        @Nullable
        public CircuitBreakerConfig.SlidingWindowType getSlidingWindowType() {
            return slidingWindowType;
        }

        public InstanceProperties setSlidingWindowType(CircuitBreakerConfig.SlidingWindowType slidingWindowType) {
            this.slidingWindowType = slidingWindowType;
            return this;
        }

        public Boolean getEnableExponentialBackoff() {
            return enableExponentialBackoff;
        }

        public InstanceProperties setEnableExponentialBackoff(Boolean enableExponentialBackoff) {
            this.enableExponentialBackoff = enableExponentialBackoff;
            return this;
        }

        @Nullable
        public Double getExponentialBackoffMultiplier() {
            return exponentialBackoffMultiplier;
        }

        public InstanceProperties setExponentialBackoffMultiplier(Double exponentialBackoffMultiplier) {
            this.exponentialBackoffMultiplier = exponentialBackoffMultiplier;
            return this;
        }

        @Nullable
        public Duration getExponentialMaxWaitDurationInOpenState() {
            return exponentialMaxWaitDurationInOpenState;
        }

        public InstanceProperties setExponentialMaxWaitDurationInOpenState(Duration exponentialMaxWaitDurationInOpenState) {
            this.exponentialMaxWaitDurationInOpenState = exponentialMaxWaitDurationInOpenState;
            return this;
        }

        @Nullable
        public Boolean getEnableRandomizedWait() {
            return enableRandomizedWait;
        }

        public InstanceProperties setEnableRandomizedWait(Boolean enableRandomizedWait) {
            this.enableRandomizedWait = enableRandomizedWait;
            return this;
        }

        @Nullable
        public Double getRandomizedWaitFactor() {
            return randomizedWaitFactor;
        }

        public InstanceProperties setRandomizedWaitFactor(Double randomizedWaitFactor) {
            this.randomizedWaitFactor = randomizedWaitFactor;
            return this;
        }
    }
}
