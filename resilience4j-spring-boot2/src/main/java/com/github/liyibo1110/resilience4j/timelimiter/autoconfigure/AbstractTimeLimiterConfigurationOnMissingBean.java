package com.github.liyibo1110.resilience4j.timelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.timelimiter.configuration.TimeLimiterConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.retry.configure.TimeLimiterConfiguration;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.spelresolver.autoconfigure.SpelResolverConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import com.github.liyibo1110.resilience4j.timelimiter.configure.ReactorTimeLimiterAspectExt;
import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterAspect;
import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterAspectExt;
import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
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
 * @date 2026-02-13 14:11
 */
@Configuration
@Import({FallbackConfigurationOnMissingBean.class, SpelResolverConfigurationOnMissingBean.class})
public class AbstractTimeLimiterConfigurationOnMissingBean {
    protected final TimeLimiterConfiguration timeLimiterConfiguration;

    protected AbstractTimeLimiterConfigurationOnMissingBean() {
        this.timeLimiterConfiguration = new TimeLimiterConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean(name="compositeTimeLimiterCustomizer")
    @Qualifier("compositeTimeLimiterCustomizer")
    public CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer(
            @Autowired(required = false) List<TimeLimiterConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeLimiterRegistry timeLimiterRegistry(
            TimeLimiterConfigurationProperties timeLimiterProperties,
            EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventsConsumerRegistry,
            RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
            @Qualifier("compositeTimeLimiterCustomizer") CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {
        return timeLimiterConfiguration.timeLimiterRegistry(
                timeLimiterProperties, timeLimiterEventsConsumerRegistry,
                timeLimiterRegistryEventConsumer, compositeTimeLimiterCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer(Optional<List<RegistryEventConsumer<TimeLimiter>>> optionalRegistryEventConsumers) {
        return timeLimiterConfiguration.timeLimiterRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @Conditional(AspectJOnClasspathCondition.class)
    @ConditionalOnMissingBean
    public TimeLimiterAspect timeLimiterAspect(
            TimeLimiterConfigurationProperties timeLimiterProperties, TimeLimiterRegistry timeLimiterRegistry,
            @Autowired(required=false) List<TimeLimiterAspectExt> timeLimiterAspectExtList,
            FallbackDecorators fallbackDecorators, SpelResolver spelResolver,
            @Autowired(required=false) ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor) {
        return timeLimiterConfiguration.timeLimiterAspect(
                timeLimiterProperties, timeLimiterRegistry, timeLimiterAspectExtList, fallbackDecorators, spelResolver, contextAwareScheduledThreadPoolExecutor);
    }

    @Bean
    @Conditional({ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorTimeLimiterAspectExt reactorTimeLimiterAspectExt() {
        return timeLimiterConfiguration.reactorTimeLimiterAspectExt();
    }
}
