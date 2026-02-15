package com.github.liyibo1110.resilience4j.ratelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterConfiguration;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-14 13:59
 */
@Configuration
@ConditionalOnClass({RateLimiter.class, RefreshScope.class})
@AutoConfigureAfter(RefreshAutoConfiguration.class)
@AutoConfigureBefore(RateLimiterAutoConfiguration.class)
public class RefreshScopedRateLimiterAutoConfiguration {
    private final RateLimiterConfiguration rateLimiterConfiguration;

    public RefreshScopedRateLimiterAutoConfiguration() {
        this.rateLimiterConfiguration = new RateLimiterConfiguration();
    }

    @Bean
    @org.springframework.cloud.context.config.annotation.RefreshScope
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry(
            RateLimiterConfigurationProperties rateLimiterProperties,
            EventConsumerRegistry<RateLimiterEvent> rateLimiterEventsConsumerRegistry,
            RegistryEventConsumer<RateLimiter> rateLimiterRegistryEventConsumer,
            @Qualifier("compositeRateLimiterCustomizer") CompositeCustomizer<RateLimiterConfigCustomizer> compositeRateLimiterCustomizer) {
        return rateLimiterConfiguration.rateLimiterRegistry(rateLimiterProperties, rateLimiterEventsConsumerRegistry,
                rateLimiterRegistryEventConsumer, compositeRateLimiterCustomizer);
    }
}
