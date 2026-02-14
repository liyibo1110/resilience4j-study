package com.github.liyibo1110.resilience4j.ratelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author liyibo
 * @date 2026-02-13 14:05
 */
public class RateLimiterConfigurationOnMissingBean extends AbstractRateLimiterConfigurationOnMissingBean {
    @Bean
    @ConditionalOnMissingBean(value=RateLimiterEvent.class, parameterizedContainer=EventConsumerRegistry.class)
    public EventConsumerRegistry<RateLimiterEvent> rateLimiterEventsConsumerRegistry() {
        return rateLimiterConfiguration.rateLimiterEventsConsumerRegistry();
    }
}
