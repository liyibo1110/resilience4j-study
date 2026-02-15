package com.github.liyibo1110.resilience4j.timelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.timelimiter.configuration.TimeLimiterConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.retry.configure.TimeLimiterConfiguration;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-14 14:00
 */
@Configuration
@ConditionalOnClass({TimeLimiter.class, RefreshScope.class})
@AutoConfigureAfter(RefreshAutoConfiguration.class)
@AutoConfigureBefore(TimeLimiterAutoConfiguration.class)
public class RefreshScopedTimeLimiterAutoConfiguration {
    private final TimeLimiterConfiguration timeLimiterConfiguration;

    public RefreshScopedTimeLimiterAutoConfiguration() {
        this.timeLimiterConfiguration = new TimeLimiterConfiguration();
    }

    @Bean
    @org.springframework.cloud.context.config.annotation.RefreshScope
    @ConditionalOnMissingBean
    public TimeLimiterRegistry timeLimiterRegistry(
            TimeLimiterConfigurationProperties prop,
            EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventsConsumerRegistry,
            RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
            @Qualifier("compositeTimeLimiterCustomizer") CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {
        return timeLimiterConfiguration.timeLimiterRegistry(prop, timeLimiterEventsConsumerRegistry,
                timeLimiterRegistryEventConsumer, compositeTimeLimiterCustomizer);
    }
}
