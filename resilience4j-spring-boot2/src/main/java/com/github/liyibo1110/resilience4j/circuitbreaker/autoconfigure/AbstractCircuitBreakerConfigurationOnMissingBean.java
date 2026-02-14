package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerAspect;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerAspectExt;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfiguration;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.ReactorCircuitBreakerAspectExt;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
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
 * @date 2026-02-13 13:48
 */
@Configuration
@Import({FallbackConfigurationOnMissingBean.class, SpelResolverConfigurationOnMissingBean.class})
public abstract class AbstractCircuitBreakerConfigurationOnMissingBean {
    protected final CircuitBreakerConfiguration circuitBreakerConfiguration;
    protected final CircuitBreakerConfigurationProperties circuitBreakerProperties;

    public AbstractCircuitBreakerConfigurationOnMissingBean(CircuitBreakerConfigurationProperties circuitBreakerProperties) {
        this.circuitBreakerProperties = circuitBreakerProperties;
        this.circuitBreakerConfiguration = new CircuitBreakerConfiguration(circuitBreakerProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name="compositeCircuitBreakerCustomizer")
    @Qualifier("compositeCircuitBreakerCustomizer")
    public CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer(
            @Autowired(required = false) List<CircuitBreakerConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
            RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
            @Qualifier("compositeCircuitBreakerCustomizer") CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer) {
        return circuitBreakerConfiguration.circuitBreakerRegistry(eventConsumerRegistry, circuitBreakerRegistryEventConsumer,
                        compositeCircuitBreakerCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<CircuitBreaker>>> optionalRegistryEventConsumers) {
        return circuitBreakerConfiguration.circuitBreakerRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(value={AspectJOnClasspathCondition.class})
    public CircuitBreakerAspect circuitBreakerAspect(CircuitBreakerRegistry circuitBreakerRegistry,
            @Autowired(required = false) List<CircuitBreakerAspectExt> circuitBreakerAspectExtList,
            FallbackDecorators fallbackDecorators, SpelResolver spelResolver) {
        return circuitBreakerConfiguration.circuitBreakerAspect(circuitBreakerRegistry, circuitBreakerAspectExtList, fallbackDecorators, spelResolver);
    }

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorCircuitBreakerAspectExt reactorCircuitBreakerAspect() {
        return circuitBreakerConfiguration.reactorCircuitBreakerAspect();
    }
}
