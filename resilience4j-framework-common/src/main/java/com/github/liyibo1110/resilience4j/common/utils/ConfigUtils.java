package com.github.liyibo1110.resilience4j.common.utils;

import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.BulkheadConfigurationProperties;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties;
import com.github.liyibo1110.resilience4j.common.ratelimiter.configuration.RateLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.common.retry.configuration.RetryConfigurationProperties;
import com.github.liyibo1110.resilience4j.common.timelimiter.configuration.TimeLimiterConfigurationProperties;

/**
 * 处理配置相关的工具
 * @author liyibo
 * @date 2026-02-09 23:08
 */
public final class ConfigUtils {

    private ConfigUtils() {}

    /**
     * 合并CircuitBreaker组件相关的属性
     */
    public static void mergePropertiesIfAny(CircuitBreakerConfigurationProperties.InstanceProperties instanceProperties,
                                            CircuitBreakerConfigurationProperties.InstanceProperties baseProperties) {
        if(instanceProperties.getRegisterHealthIndicator() == null && baseProperties.getRegisterHealthIndicator() != null)
            instanceProperties.setRegisterHealthIndicator(baseProperties.getRegisterHealthIndicator());

        if(instanceProperties.getAllowHealthIndicatorToFail() == null && baseProperties.getAllowHealthIndicatorToFail() != null)
            instanceProperties.setAllowHealthIndicatorToFail(baseProperties.getAllowHealthIndicatorToFail());

        if(instanceProperties.getEventConsumerBufferSize() == null && baseProperties.getEventConsumerBufferSize() != null)
            instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
    }

    /**
     * 合并Bulkhead组件相关的属性
     */
    public static void mergePropertiesIfAny(BulkheadConfigurationProperties.InstanceProperties baseProperties,
                                            BulkheadConfigurationProperties.InstanceProperties instanceProperties) {
        if(instanceProperties.getEventConsumerBufferSize() == null && baseProperties.getEventConsumerBufferSize() != null)
            instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
    }

    /**
     * 合并RateLimiter组件相关的属性
     */
    public static void mergePropertiesIfAny(RateLimiterConfigurationProperties.InstanceProperties baseProperties,
                                            RateLimiterConfigurationProperties.InstanceProperties instanceProperties) {
        if(instanceProperties.getRegisterHealthIndicator() == null && baseProperties.getRegisterHealthIndicator() != null)
            instanceProperties.setRegisterHealthIndicator(baseProperties.getRegisterHealthIndicator());

        if(instanceProperties.getAllowHealthIndicatorToFail() == null && baseProperties.getAllowHealthIndicatorToFail() != null)
            instanceProperties.setAllowHealthIndicatorToFail(baseProperties.getAllowHealthIndicatorToFail());

        if(instanceProperties.getSubscribeForEvents() == null && baseProperties.getSubscribeForEvents() != null)
            instanceProperties.setSubscribeForEvents(baseProperties.getSubscribeForEvents());

        if(instanceProperties.getEventConsumerBufferSize() == null && baseProperties.getEventConsumerBufferSize() != null)
            instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
    }

    /**
     * 合并Retry组件相关的属性
     */
    public static void mergePropertiesIfAny(RetryConfigurationProperties.InstanceProperties baseProperties,
                                            RetryConfigurationProperties.InstanceProperties instanceProperties) {
        if(instanceProperties.getEnableExponentialBackoff() == null && baseProperties.getEnableExponentialBackoff() != null)
            instanceProperties.setEnableExponentialBackoff(baseProperties.getEnableExponentialBackoff());

        if(instanceProperties.getEnableRandomizedWait() == null && baseProperties.getEnableRandomizedWait() != null)
            instanceProperties.setEnableRandomizedWait(baseProperties.getEnableRandomizedWait());

        if(instanceProperties.getExponentialBackoffMultiplier() == null && baseProperties.getExponentialBackoffMultiplier() != null)
            instanceProperties.setExponentialBackoffMultiplier(baseProperties.getExponentialBackoffMultiplier());

        if(instanceProperties.getExponentialMaxWaitDuration() == null && baseProperties.getExponentialMaxWaitDuration() != null)
            instanceProperties.setExponentialMaxWaitDuration(baseProperties.getExponentialMaxWaitDuration());
    }

    /**
     * 合并TimeLimiter组件相关的属性
     */
    public static void mergePropertiesIfAny(TimeLimiterConfigurationProperties.InstanceProperties baseProperties,
                                            TimeLimiterConfigurationProperties.InstanceProperties instanceProperties) {
        if(instanceProperties.getEventConsumerBufferSize() == null && baseProperties.getEventConsumerBufferSize() != null)
            instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
    }
}
