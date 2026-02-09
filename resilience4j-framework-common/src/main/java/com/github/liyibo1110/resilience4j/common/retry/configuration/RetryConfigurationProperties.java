package com.github.liyibo1110.resilience4j.common.retry.configuration;

import com.github.liyibo1110.resilience4j.common.CommonProperties;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.utils.ConfigUtils;
import com.github.liyibo1110.resilience4j.core.ClassUtils;
import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.IntervalBiFunction;
import com.github.liyibo1110.resilience4j.core.IntervalFunction;
import com.github.liyibo1110.resilience4j.core.StringUtils;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.retry.RetryConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author liyibo
 * @date 2026-02-09 23:35
 */
public class RetryConfigurationProperties extends CommonProperties {
    private final Map<String, InstanceProperties> instances = new HashMap<>();
    private Map<String, InstanceProperties> configs = new HashMap<>();

    public RetryConfig createRetryConfig(String backend,
                                         CompositeCustomizer<RetryConfigCustomizer> customizer) {
        return createRetryConfig(getBackendProperties(backend), customizer, backend);
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

    public RetryConfig createRetryConfig(InstanceProperties instanceProperties,
                                         CompositeCustomizer<RetryConfigCustomizer> customizer, String backend) {
        if (instanceProperties != null && StringUtils
                .isNotEmpty(instanceProperties.getBaseConfig())) {
            InstanceProperties baseProperties = configs.get(instanceProperties.getBaseConfig());
            if (baseProperties == null) {
                throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
            }
            return buildConfigFromBaseConfig(baseProperties, instanceProperties,
                    customizer, backend);
        }
        return buildRetryConfig(RetryConfig.custom(), instanceProperties, customizer,
                backend);
    }

    private RetryConfig buildConfigFromBaseConfig(InstanceProperties baseProperties, InstanceProperties instanceProperties,
                                                  CompositeCustomizer<RetryConfigCustomizer> customizer, String backend) {
        RetryConfig baseConfig = createRetryConfig(baseProperties,
                customizer, backend);
        ConfigUtils.mergePropertiesIfAny(baseProperties, instanceProperties);
        return buildRetryConfig(RetryConfig.from(baseConfig), instanceProperties,
                customizer, backend);
    }

    private RetryConfig buildRetryConfig(RetryConfig.Builder builder, InstanceProperties properties,
                                         CompositeCustomizer<RetryConfigCustomizer> customizer, String backend) {
        if(properties == null)
            return builder.build();

        configureRetryIntervalFunction(properties, builder);

        if(properties.getMaxRetryAttempts() != null && properties.getMaxRetryAttempts() != 0)
            builder.maxAttempts(properties.getMaxRetryAttempts());

        if(properties.getMaxAttempts() != null && properties.getMaxAttempts() != 0)
            builder.maxAttempts(properties.getMaxAttempts());

        if(properties.getRetryExceptionPredicate() != null) {
            Predicate<Throwable> predicate = ClassUtils.instantiatePredicateClass(properties.getRetryExceptionPredicate());
            builder.retryOnException(predicate);
        }

        if(properties.getIgnoreExceptions() != null)
            builder.ignoreExceptions(properties.getIgnoreExceptions());

        if(properties.getRetryExceptions() != null)
            builder.retryExceptions(properties.getRetryExceptions());

        if(properties.getResultPredicate() != null) {
            Predicate<Object> predicate = ClassUtils.instantiatePredicateClass(properties.getResultPredicate());
            builder.retryOnResult(predicate);
        }
        if(properties.getIntervalBiFunction() != null) {
            IntervalBiFunction<Object> intervalBiFunction = ClassUtils.instantiateIntervalBiFunctionClass(properties.getIntervalBiFunction());
            builder.intervalBiFunction(intervalBiFunction);
        }

        customizer.getCustomizer(backend).ifPresent(c -> c.customize(builder));
        return builder.build();
    }

    private void configureRetryIntervalFunction(InstanceProperties properties, RetryConfig.Builder<Object> builder) {
        // these take precedence over deprecated properties. Setting one or the other will still work.
        if(properties.getWaitDuration() != null && properties.getWaitDuration().toMillis() > 0) {
            if (Boolean.TRUE.equals(properties.getEnableExponentialBackoff()) && Boolean.TRUE.equals(properties.getEnableRandomizedWait()))
                configureExponentialBackoffAndRandomizedWait(properties, builder);
            else if (Boolean.TRUE.equals(properties.getEnableExponentialBackoff()))
                configureExponentialBackoff(properties, builder);
            else if (Boolean.TRUE.equals(properties.getEnableRandomizedWait()))
                configureRandomizedWait(properties, builder);
            else
                builder.waitDuration(properties.getWaitDuration());
        }
    }

    private void configureExponentialBackoffAndRandomizedWait(InstanceProperties properties, RetryConfig.Builder<Object> builder) {
        Duration waitDuration = properties.getWaitDuration();
        Double backoffMultiplier = properties.getExponentialBackoffMultiplier();
        Double randomizedWaitFactor = properties.getRandomizedWaitFactor();
        Duration maxWaitDuration = properties.getExponentialMaxWaitDuration();
        if (maxWaitDuration != null && randomizedWaitFactor != null && backoffMultiplier != null)
            builder.intervalFunction(IntervalFunction.ofExponentialRandomBackoff(waitDuration, backoffMultiplier, randomizedWaitFactor, maxWaitDuration));
        else if (randomizedWaitFactor != null && backoffMultiplier != null)
            builder.intervalFunction(IntervalFunction.ofExponentialRandomBackoff(waitDuration, backoffMultiplier, randomizedWaitFactor));
        else if (backoffMultiplier != null)
            builder.intervalFunction(IntervalFunction.ofExponentialRandomBackoff(waitDuration, backoffMultiplier));
        else
            builder.intervalFunction(IntervalFunction.ofExponentialRandomBackoff(waitDuration));
    }

    private void configureExponentialBackoff(InstanceProperties properties, RetryConfig.Builder<Object> builder) {
        Duration waitDuration = properties.getWaitDuration();
        Double backoffMultiplier = properties.getExponentialBackoffMultiplier();
        Duration maxWaitDuration = properties.getExponentialMaxWaitDuration();
        if (maxWaitDuration != null && backoffMultiplier != null)
            builder.intervalFunction(IntervalFunction.ofExponentialBackoff(waitDuration, backoffMultiplier, maxWaitDuration));
        else if (backoffMultiplier != null)
            builder.intervalFunction(IntervalFunction.ofExponentialBackoff(waitDuration, backoffMultiplier));
        else
            builder.intervalFunction(IntervalFunction.ofExponentialBackoff(waitDuration));
    }

    private void configureRandomizedWait(InstanceProperties properties, RetryConfig.Builder<Object> builder) {
        Duration waitDuration = properties.getWaitDuration();
        Double randomizedWaitFactor = properties.getRandomizedWaitFactor();
        if(randomizedWaitFactor != null)
            builder.intervalFunction(IntervalFunction.ofRandomized(waitDuration, randomizedWaitFactor));
        else
            builder.intervalFunction(IntervalFunction.ofRandomized(waitDuration));
    }

    public static class InstanceProperties {
        @Nullable
        private Duration waitDuration;

        @Nullable
        private Class<? extends IntervalBiFunction<Object>> intervalBiFunction;

        @Nullable
        @Deprecated
        private Integer maxRetryAttempts;

        @Nullable
        private Integer maxAttempts;

        @Nullable
        private Class<? extends Predicate<Throwable>> retryExceptionPredicate;

        @Nullable
        private Class<? extends Predicate<Object>> resultPredicate;

        @Nullable
        private Class<? extends Throwable>[] retryExceptions;

        @Nullable
        private Class<? extends Throwable>[] ignoreExceptions;

        @Nullable
        private Integer eventConsumerBufferSize;

        @Nullable
        private Boolean enableExponentialBackoff;

        private Double exponentialBackoffMultiplier;

        private Duration exponentialMaxWaitDuration;

        @Nullable
        private Boolean enableRandomizedWait;

        private Double randomizedWaitFactor;

        @Nullable
        private String baseConfig;

        @Nullable
        public Duration getWaitDuration() {
            return waitDuration;
        }

        public InstanceProperties setWaitDuration(Duration waitDuration) {
            Objects.requireNonNull(waitDuration);
            if (waitDuration.toMillis() < 0) {
                throw new IllegalArgumentException(
                        "waitDuration must be a positive value");
            }

            this.waitDuration = waitDuration;
            return this;
        }

        @Nullable
        public Class<? extends IntervalBiFunction<Object>> getIntervalBiFunction() {
            return intervalBiFunction;
        }

        public void setIntervalBiFunction(Class<? extends IntervalBiFunction<Object>> intervalBiFunction) {
            this.intervalBiFunction = intervalBiFunction;
        }

        /**
         *
         * @deprecated use getMaxAttempts()
         */
        @Nullable
        @Deprecated
        public Integer getMaxRetryAttempts() {
            return maxRetryAttempts;
        }

        @Nullable
        public Integer getMaxAttempts() {
            return maxAttempts;
        }

        @Deprecated
        public InstanceProperties setMaxRetryAttempts(Integer maxRetryAttempts) {
            Objects.requireNonNull(maxRetryAttempts);
            if(maxRetryAttempts < 1)
                throw new IllegalArgumentException("maxRetryAttempts must be greater than or equal to 1.");
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        public InstanceProperties setMaxAttempts(Integer maxAttempts) {
            Objects.requireNonNull(maxAttempts);
            if(maxAttempts < 1)
                throw new IllegalArgumentException("maxAttempts must be greater than or equal to 1.");
            this.maxAttempts = maxAttempts;
            return this;
        }

        @Nullable
        public Class<? extends Predicate<Throwable>> getRetryExceptionPredicate() {
            return retryExceptionPredicate;
        }

        public InstanceProperties setRetryExceptionPredicate(Class<? extends Predicate<Throwable>> retryExceptionPredicate) {
            this.retryExceptionPredicate = retryExceptionPredicate;
            return this;
        }

        @Nullable
        public Class<? extends Predicate<Object>> getResultPredicate() {
            return resultPredicate;
        }

        public InstanceProperties setResultPredicate(Class<? extends Predicate<Object>> resultPredicate) {
            this.resultPredicate = resultPredicate;
            return this;
        }

        @Nullable
        public Class<? extends Throwable>[] getRetryExceptions() {
            return retryExceptions;
        }

        public InstanceProperties setRetryExceptions(Class<? extends Throwable>[] retryExceptions) {
            this.retryExceptions = retryExceptions;
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
        public Duration getExponentialMaxWaitDuration() {
            return exponentialMaxWaitDuration;
        }

        public InstanceProperties setExponentialMaxWaitDuration(Duration exponentialMaxWaitDuration) {
            this.exponentialMaxWaitDuration = exponentialMaxWaitDuration;
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
