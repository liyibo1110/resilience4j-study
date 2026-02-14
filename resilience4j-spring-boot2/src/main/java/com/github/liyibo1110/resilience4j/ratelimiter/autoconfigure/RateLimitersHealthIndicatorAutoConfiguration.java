package com.github.liyibo1110.resilience4j.ratelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.ratelimiter.monitoring.health.RateLimitersHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-13 14:06
 */
@Configuration
@ConditionalOnClass({RateLimiter.class, HealthIndicator.class, StatusAggregator.class})
@AutoConfigureAfter(RateLimiterAutoConfiguration.class)
@AutoConfigureBefore(HealthContributorAutoConfiguration.class)
public class RateLimitersHealthIndicatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name="rateLimitersHealthIndicator")
    @ConditionalOnProperty(prefix="management.health.ratelimiters", name="enabled")
    public RateLimitersHealthIndicator rateLimitersHealthIndicator(
            RateLimiterRegistry rateLimiterRegistry, RateLimiterConfigurationProperties rateLimiterProperties,
            StatusAggregator statusAggregator) {
        return new RateLimitersHealthIndicator(rateLimiterRegistry, rateLimiterProperties, statusAggregator);
    }
}
