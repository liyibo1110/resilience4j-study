package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-13 13:51
 */
@Configuration
public class CircuitBreakerConfigurationOnMissingBean extends AbstractCircuitBreakerConfigurationOnMissingBean {
    public CircuitBreakerConfigurationOnMissingBean(CircuitBreakerConfigurationProperties circuitBreakerProperties) {
        super(circuitBreakerProperties);
    }

    @Bean
    @ConditionalOnMissingBean(value=CircuitBreakerEvent.class, parameterizedContainer=EventConsumerRegistry.class)
    public EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry() {
        return circuitBreakerConfiguration.eventConsumerRegistry();
    }
}
