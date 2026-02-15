package com.github.liyibo1110.resilience4j.retry.autoconfigure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import com.github.liyibo1110.resilience4j.retry.configure.RetryConfiguration;
import com.github.liyibo1110.resilience4j.retry.configure.RetryConfigurationProperties;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
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
@ConditionalOnClass({Retry.class, RefreshScope.class})
@AutoConfigureAfter(RefreshAutoConfiguration.class)
@AutoConfigureBefore(RetryAutoConfiguration.class)
public class RefreshScopedRetryAutoConfiguration {
    private final RetryConfiguration retryConfiguration;

    protected RefreshScopedRetryAutoConfiguration() {
        this.retryConfiguration = new RetryConfiguration();
    }

    @Bean
    @org.springframework.cloud.context.config.annotation.RefreshScope
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(RetryConfigurationProperties retryConfigurationProperties,
                                       EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry,
                                       RegistryEventConsumer<Retry> retryRegistryEventConsumer,
                                       @Qualifier("compositeRetryCustomizer") CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer) {
        return retryConfiguration.retryRegistry(retryConfigurationProperties, retryEventConsumerRegistry,
                        retryRegistryEventConsumer, compositeRetryCustomizer);
    }
}
