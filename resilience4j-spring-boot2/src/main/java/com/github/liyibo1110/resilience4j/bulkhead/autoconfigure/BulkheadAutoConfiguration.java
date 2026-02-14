package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.bulkhead.monitoring.endpoint.BulkheadEndpoint;
import com.github.liyibo1110.resilience4j.bulkhead.monitoring.endpoint.BulkheadEventsEndpoint;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author liyibo
 * @date 2026-02-13 14:02
 */
@Configuration
@ConditionalOnClass(Bulkhead.class)
@EnableConfigurationProperties({BulkheadProperties.class, ThreadPoolBulkheadProperties.class})
@Import({BulkheadConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class BulkheadAutoConfiguration {

    @Configuration
    @ConditionalOnClass(Endpoint.class)
    static class BulkheadEndpointAutoConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        public BulkheadEndpoint bulkheadEndpoint(BulkheadRegistry bulkheadRegistry, ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry) {
            return new BulkheadEndpoint(bulkheadRegistry, threadPoolBulkheadRegistry);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public BulkheadEventsEndpoint bulkheadEventsEndpoint(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry) {
            return new BulkheadEventsEndpoint(eventConsumerRegistry);
        }
    }
}
