package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-13 14:01
 */
@Configuration
public class BulkheadConfigurationOnMissingBean extends AbstractBulkheadConfigurationOnMissingBean {
    @Bean
    @ConditionalOnMissingBean(value=BulkheadEvent.class, parameterizedContainer=EventConsumerRegistry.class)
    public EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry() {
        return bulkheadConfiguration.bulkheadEventConsumerRegistry();
    }
}
