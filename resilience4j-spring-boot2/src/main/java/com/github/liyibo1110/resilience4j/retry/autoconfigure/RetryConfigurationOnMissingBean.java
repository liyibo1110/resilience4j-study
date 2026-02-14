package com.github.liyibo1110.resilience4j.retry.autoconfigure;

import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-13 14:09
 */
@Configuration
public class RetryConfigurationOnMissingBean extends AbstractRetryConfigurationOnMissingBean {
    @Bean
    @ConditionalOnMissingBean(value=RetryEvent.class, parameterizedContainer=EventConsumerRegistry.class)
    public EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry() {
        return retryConfiguration.retryEventConsumerRegistry();
    }
}
