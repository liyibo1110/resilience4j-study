package com.github.liyibo1110.resilience4j.timelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import com.github.liyibo1110.resilience4j.timelimiter.monitoring.endpoint.TimeLimiterEndpoint;
import com.github.liyibo1110.resilience4j.timelimiter.monitoring.endpoint.TimeLimiterEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author liyibo
 * @date 2026-02-13 14:14
 */
@Configuration
@ConditionalOnClass(TimeLimiter.class)
@EnableConfigurationProperties(TimeLimiterProperties.class)
@Import({TimeLimiterConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class TimeLimiterAutoConfiguration {
    @Configuration
    @ConditionalOnClass(Endpoint.class)
    static class TimeLimiterAutoEndpointConfiguration {

        @Bean
        @ConditionalOnAvailableEndpoint
        public TimeLimiterEndpoint timeLimiterEndpoint(TimeLimiterRegistry timeLimiterRegistry) {
            return new TimeLimiterEndpoint(timeLimiterRegistry);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public TimeLimiterEventsEndpoint timeLimiterEventsEndpoint(EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry) {
            return new TimeLimiterEventsEndpoint(eventConsumerRegistry);
        }
    }
}
