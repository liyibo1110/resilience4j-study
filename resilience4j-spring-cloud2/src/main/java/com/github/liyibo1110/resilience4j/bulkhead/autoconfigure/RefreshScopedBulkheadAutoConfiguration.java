package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.configure.BulkheadConfiguration;
import com.github.liyibo1110.resilience4j.bulkhead.configure.BulkheadConfigurationProperties;
import com.github.liyibo1110.resilience4j.bulkhead.configure.threadpool.ThreadPoolBulkheadConfiguration;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigurationProperties;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
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
 * @date 2026-02-14 13:58
 */
@Configuration
@ConditionalOnClass({Bulkhead.class, RefreshScope.class})
@AutoConfigureAfter(RefreshAutoConfiguration.class)
@AutoConfigureBefore(BulkheadAutoConfiguration.class)
public class RefreshScopedBulkheadAutoConfiguration {
    protected final BulkheadConfiguration bulkheadConfiguration;
    protected final ThreadPoolBulkheadConfiguration threadPoolBulkheadConfiguration;

    protected RefreshScopedBulkheadAutoConfiguration() {
        this.threadPoolBulkheadConfiguration = new ThreadPoolBulkheadConfiguration();
        this.bulkheadConfiguration = new BulkheadConfiguration();
    }

    @Bean
    @org.springframework.cloud.context.config.annotation.RefreshScope
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry(
            BulkheadConfigurationProperties bulkheadConfigurationProperties,
            EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
            RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
            @Qualifier("compositeBulkheadCustomizer") CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
        return bulkheadConfiguration.bulkheadRegistry(bulkheadConfigurationProperties, bulkheadEventConsumerRegistry,
                        bulkheadRegistryEventConsumer, compositeBulkheadCustomizer);
    }

    @Bean
    @org.springframework.cloud.context.config.annotation.RefreshScope
    @ConditionalOnMissingBean
    public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry(
            ThreadPoolBulkheadConfigurationProperties threadPoolBulkheadConfigurationProperties,
            EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
            RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
            @Qualifier("compositeThreadPoolBulkheadCustomizer") CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {

        return threadPoolBulkheadConfiguration.threadPoolBulkheadRegistry(
                threadPoolBulkheadConfigurationProperties, bulkheadEventConsumerRegistry,
                threadPoolBulkheadRegistryEventConsumer, compositeThreadPoolBulkheadCustomizer);
    }
}
