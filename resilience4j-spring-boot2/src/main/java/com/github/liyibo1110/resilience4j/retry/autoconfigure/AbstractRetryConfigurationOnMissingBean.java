package com.github.liyibo1110.resilience4j.retry.autoconfigure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import com.github.liyibo1110.resilience4j.retry.configure.ReactorRetryAspectExt;
import com.github.liyibo1110.resilience4j.retry.configure.RetryAspect;
import com.github.liyibo1110.resilience4j.retry.configure.RetryAspectExt;
import com.github.liyibo1110.resilience4j.retry.configure.RetryConfiguration;
import com.github.liyibo1110.resilience4j.retry.configure.RetryConfigurationProperties;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.spelresolver.autoconfigure.SpelResolverConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.utils.AspectJOnClasspathCondition;
import com.github.liyibo1110.resilience4j.utils.ReactorOnClasspathCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Optional;

/**
 * @author liyibo
 * @date 2026-02-13 14:07
 */
@Configuration
@Import({FallbackConfigurationOnMissingBean.class, SpelResolverConfigurationOnMissingBean.class})
public class AbstractRetryConfigurationOnMissingBean {
    protected final RetryConfiguration retryConfiguration;

    public AbstractRetryConfigurationOnMissingBean() {
        this.retryConfiguration = new RetryConfiguration();
    }

    @Bean
    @Qualifier("compositeRetryCustomizer")
    @ConditionalOnMissingBean(name="compositeRetryCustomizer")
    public CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer(@Autowired(required=false) List<RetryConfigCustomizer> configCustomizers) {
        return new CompositeCustomizer<>(configCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(RetryConfigurationProperties retryConfigurationProperties,
                                       EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry,
                                       RegistryEventConsumer<Retry> retryRegistryEventConsumer,
                                       @Qualifier("compositeRetryCustomizer") CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer) {
        return retryConfiguration.retryRegistry(retryConfigurationProperties, retryEventConsumerRegistry,
                        retryRegistryEventConsumer, compositeRetryCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<Retry> retryRegistryEventConsumer(Optional<List<RegistryEventConsumer<Retry>>> optionalRegistryEventConsumers) {
        return retryConfiguration.retryRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public RetryAspect retryAspect(
            RetryConfigurationProperties retryConfigurationProperties, RetryRegistry retryRegistry,
            @Autowired(required = false) List<RetryAspectExt> retryAspectExtList,
            FallbackDecorators fallbackDecorators, SpelResolver spelResolver,
            @Autowired(required = false) ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor) {
        return retryConfiguration.retryAspect(retryConfigurationProperties, retryRegistry, retryAspectExtList,
                        fallbackDecorators, spelResolver, contextAwareScheduledThreadPoolExecutor);
    }

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorRetryAspectExt reactorRetryAspectExt() {
        return retryConfiguration.reactorRetryAspectExt();
    }
}
