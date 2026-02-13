package com.github.liyibo1110.resilience4j.retry.configure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.DefaultEventConsumerRegistry;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import com.github.liyibo1110.resilience4j.core.registry.CompositeRegistryEventConsumer;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryConfig;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.utils.AspectJOnClasspathCondition;
import com.github.liyibo1110.resilience4j.utils.ReactorOnClasspathCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-13 11:54
 */
@Configuration
public class RetryConfiguration {

    @Bean
    @Qualifier("compositeRetryCustomizer")
    public CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer(
            @Autowired(required = false) List<RetryConfigCustomizer> configCustomizers) {
        return new CompositeCustomizer<>(configCustomizers);
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfigurationProperties prop,
                                       EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry,
                                       RegistryEventConsumer<Retry> retryRegistryEventConsumer,
                                       @Qualifier("compositeRetryCustomizer") CompositeCustomizer<RetryConfigCustomizer> customizer) {
        RetryRegistry retryRegistry = createRetryRegistry(prop, retryRegistryEventConsumer, customizer);
        registerEventConsumer(retryRegistry, retryEventConsumerRegistry, prop);
        prop.getInstances()
                .forEach((name, properties) -> retryRegistry.retry(name, prop.createRetryConfig(name, customizer)));
        return retryRegistry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<Retry> retryRegistryEventConsumer(Optional<List<RegistryEventConsumer<Retry>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    private RetryRegistry createRetryRegistry(
            RetryConfigurationProperties prop, RegistryEventConsumer<Retry> retryRegistryEventConsumer,
            CompositeCustomizer<RetryConfigCustomizer> customizer) {
        Map<String, RetryConfig> configs = prop.getConfigs()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> prop.createRetryConfig(entry.getValue(), customizer, entry.getKey())));

        return RetryRegistry.of(configs, retryRegistryEventConsumer,
                io.vavr.collection.HashMap.ofAll(prop.getTags()));
    }

    private void registerEventConsumer(RetryRegistry retryRegistry,
                                       EventConsumerRegistry<RetryEvent> eventConsumerRegistry,
                                       RetryConfigurationProperties prop) {
        retryRegistry.getEventPublisher()
                .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), prop))
                .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), prop));
    }

    private void registerEventConsumer(EventConsumerRegistry<RetryEvent> eventConsumerRegistry,
                                       Retry retry, RetryConfigurationProperties prop) {
        int eventConsumerBufferSize = Optional
                .ofNullable(prop.getBackendProperties(retry.getName()))
                .map(com.github.liyibo1110.resilience4j.common.retry.configuration.RetryConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                .orElse(100);
        retry.getEventPublisher().onEvent(eventConsumerRegistry.createEventConsumer(retry.getName(), eventConsumerBufferSize));
    }

    @Bean
    @Conditional(value={AspectJOnClasspathCondition.class})
    public RetryAspect retryAspect(
            RetryConfigurationProperties prop, RetryRegistry retryRegistry,
            @Autowired(required = false) List<RetryAspectExt> retryAspectExtList,
            FallbackDecorators fallbackDecorators, SpelResolver spelResolver,
            @Autowired(required = false) ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor) {
        return new RetryAspect(prop, retryRegistry, retryAspectExtList, fallbackDecorators, spelResolver, contextAwareScheduledThreadPoolExecutor);
    }

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public ReactorRetryAspectExt reactorRetryAspectExt() {
        return new ReactorRetryAspectExt();
    }

    @Bean
    public EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }
}
