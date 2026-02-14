package com.github.liyibo1110.resilience4j.ratelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterAspect;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterAspectExt;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterConfiguration;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.ReactorRateLimiterAspectExt;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
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
 * @date 2026-02-13 14:04
 */
@Configuration
@Import({FallbackConfigurationOnMissingBean.class, SpelResolverConfigurationOnMissingBean.class})
public abstract class AbstractRateLimiterConfigurationOnMissingBean {
    protected final RateLimiterConfiguration rateLimiterConfiguration;

    public AbstractRateLimiterConfigurationOnMissingBean() {
        this.rateLimiterConfiguration = new RateLimiterConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeRateLimiterCustomizer")
    @Qualifier("compositeRateLimiterCustomizer")
    public CompositeCustomizer<RateLimiterConfigCustomizer> compositeRateLimiterCustomizer(
            @Autowired(required = false) List<RateLimiterConfigCustomizer> configCustomizers) {
        return new CompositeCustomizer<>(configCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry(
            RateLimiterConfigurationProperties rateLimiterProperties,
            EventConsumerRegistry<RateLimiterEvent> rateLimiterEventsConsumerRegistry,
            RegistryEventConsumer<RateLimiter> rateLimiterRegistryEventConsumer,
            @Qualifier("compositeRateLimiterCustomizer") CompositeCustomizer<RateLimiterConfigCustomizer> compositeRateLimiterCustomizer) {
        return rateLimiterConfiguration.rateLimiterRegistry(rateLimiterProperties, rateLimiterEventsConsumerRegistry,
                        rateLimiterRegistryEventConsumer, compositeRateLimiterCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<RateLimiter> rateLimiterRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<RateLimiter>>> optionalRegistryEventConsumers) {
        return rateLimiterConfiguration.rateLimiterRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public RateLimiterAspect rateLimiterAspect(
            RateLimiterConfigurationProperties rateLimiterProperties,
            RateLimiterRegistry rateLimiterRegistry,
            @Autowired(required = false) List<RateLimiterAspectExt> rateLimiterAspectExtList,
            FallbackDecorators fallbackDecorators, SpelResolver spelResolver) {
        return rateLimiterConfiguration.rateLimiterAspect(rateLimiterProperties, rateLimiterRegistry, rateLimiterAspectExtList,
                        fallbackDecorators, spelResolver);
    }

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorRateLimiterAspectExt reactorRateLimiterAspectExt() {
        return rateLimiterConfiguration.reactorRateLimiterAspectExt();
    }
}
