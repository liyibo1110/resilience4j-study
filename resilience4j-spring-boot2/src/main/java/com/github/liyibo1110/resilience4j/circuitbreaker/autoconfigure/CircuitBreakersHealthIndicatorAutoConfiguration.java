package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.health.CircuitBreakersHealthIndicator;
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
 * @date 2026-02-13 13:56
 */
@Configuration
@ConditionalOnClass({CircuitBreaker.class, HealthIndicator.class, StatusAggregator.class})
@AutoConfigureAfter(CircuitBreakerAutoConfiguration.class)
@AutoConfigureBefore(HealthContributorAutoConfiguration.class)
public class CircuitBreakersHealthIndicatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name="circuitBreakersHealthIndicator")
    @ConditionalOnProperty(prefix="management.health.circuitbreakers", name="enabled")
    public CircuitBreakersHealthIndicator circuitBreakersHealthIndicator(
            CircuitBreakerRegistry circuitBreakerRegistry,
            CircuitBreakerConfigurationProperties circuitBreakerProperties,
            StatusAggregator statusAggregator) {
        return new CircuitBreakersHealthIndicator(circuitBreakerRegistry, circuitBreakerProperties, statusAggregator);
    }

}
