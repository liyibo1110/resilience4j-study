package com.github.liyibo1110.resilience4j.timelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-13 14:13
 */
@Configuration
public class TimeLimiterConfigurationOnMissingBean extends AbstractTimeLimiterConfigurationOnMissingBean {
    @Bean
    @ConditionalOnMissingBean(value=TimeLimiterEvent.class, parameterizedContainer=EventConsumerRegistry.class)
    public EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventsConsumerRegistry() {
        return timeLimiterConfiguration.timeLimiterEventsConsumerRegistry();
    }
}
