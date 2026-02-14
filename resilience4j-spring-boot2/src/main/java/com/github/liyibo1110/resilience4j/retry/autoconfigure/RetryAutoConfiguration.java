package com.github.liyibo1110.resilience4j.retry.autoconfigure;

import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import com.github.liyibo1110.resilience4j.retry.monitoring.endpoint.RetryEndpoint;
import com.github.liyibo1110.resilience4j.retry.monitoring.endpoint.RetryEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author liyibo
 * @date 2026-02-13 14:09
 */
@Configuration
@ConditionalOnClass(RetryAutoConfiguration.class)
@EnableConfigurationProperties(RetryProperties.class)
@Import({RetryConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class RetryAutoConfiguration {
    @Configuration
    @ConditionalOnClass(Endpoint.class)
    static class RetryAutoEndpointConfiguration {

        @Bean
        @ConditionalOnAvailableEndpoint
        public RetryEndpoint retryEndpoint(RetryRegistry retryRegistry) {
            return new RetryEndpoint(retryRegistry);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public RetryEventsEndpoint retryEventsEndpoint(EventConsumerRegistry<RetryEvent> eventConsumerRegistry) {
            return new RetryEventsEndpoint(eventConsumerRegistry);
        }
    }
}
