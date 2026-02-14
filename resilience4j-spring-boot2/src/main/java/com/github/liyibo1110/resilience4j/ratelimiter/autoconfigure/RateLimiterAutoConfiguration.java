package com.github.liyibo1110.resilience4j.ratelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.monitoring.endpoint.RateLimiterEndpoint;
import com.github.liyibo1110.resilience4j.ratelimiter.monitoring.endpoint.RateLimiterEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author liyibo
 * @date 2026-02-13 14:06
 */
@Configuration
@ConditionalOnClass(RateLimiter.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimiterConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class RateLimiterAutoConfiguration {

    @Configuration
    @ConditionalOnClass( Endpoint.class)
    static class RateLimiterEndpointAutoConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        public RateLimiterEndpoint rateLimiterEndpoint(RateLimiterRegistry rateLimiterRegistry) {
            return new RateLimiterEndpoint(rateLimiterRegistry);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public RateLimiterEventsEndpoint rateLimiterEventsEndpoint(EventConsumerRegistry<RateLimiterEvent> eventConsumerRegistry) {
            return new RateLimiterEventsEndpoint(eventConsumerRegistry);
        }
    }
}
