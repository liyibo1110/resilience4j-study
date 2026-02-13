package com.github.liyibo1110.resilience4j.bulkhead.configure;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadConfig;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.configure.threadpool.ThreadPoolBulkheadConfiguration;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.DefaultEventConsumerRegistry;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.CompositeRegistryEventConsumer;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.configure.FallbackConfiguration;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.spelresolver.configure.SpelResolverConfiguration;
import com.github.liyibo1110.resilience4j.utils.AspectJOnClasspathCondition;
import com.github.liyibo1110.resilience4j.utils.ReactorOnClasspathCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-13 11:45
 */
@Configuration
@Import({ThreadPoolBulkheadConfiguration.class, FallbackConfiguration.class, SpelResolverConfiguration.class})
public class BulkheadConfiguration {

    @Bean
    @Qualifier("compositeBulkheadCustomizer")
    public CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer(
            @Autowired(required = false) List<BulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry(
            BulkheadConfigurationProperties prop,
            EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
            RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
            @Qualifier("compositeBulkheadCustomizer") CompositeCustomizer<BulkheadConfigCustomizer> customizer) {
        BulkheadRegistry bulkheadRegistry = createBulkheadRegistry(prop,
                bulkheadRegistryEventConsumer, customizer);
        registerEventConsumer(bulkheadRegistry, bulkheadEventConsumerRegistry, prop);
        prop.getInstances().forEach((name, properties) ->
                bulkheadRegistry.bulkhead(name, prop.createBulkheadConfig(properties, customizer, name)));
        return bulkheadRegistry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<Bulkhead>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(
                optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    private BulkheadRegistry createBulkheadRegistry(
            BulkheadConfigurationProperties prop,
            RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
            CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
        Map<String, BulkheadConfig> configs = prop.getConfigs()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> prop.createBulkheadConfig(entry.getValue(), compositeBulkheadCustomizer, entry.getKey())));
        return BulkheadRegistry.of(configs, bulkheadRegistryEventConsumer,
                io.vavr.collection.HashMap.ofAll(prop.getTags()));
    }

    private void registerEventConsumer(BulkheadRegistry bulkheadRegistry,
                                       EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                       BulkheadConfigurationProperties properties) {
        bulkheadRegistry.getEventPublisher()
                .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
                .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
    }

    private void registerEventConsumer(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                       Bulkhead bulkHead, BulkheadConfigurationProperties prop) {
        int eventConsumerBufferSize = Optional.ofNullable(prop.getBackendProperties(bulkHead.getName()))
                .map(com.github.liyibo1110.resilience4j.common.bulkhead.configuration.BulkheadConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                .orElse(100);
        bulkHead.getEventPublisher().onEvent(eventConsumerRegistry.createEventConsumer(bulkHead.getName(), eventConsumerBufferSize));
    }

    @Bean
    @Conditional(value={AspectJOnClasspathCondition.class})
    public BulkheadAspect bulkheadAspect(
            BulkheadConfigurationProperties prop,
            ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
            BulkheadRegistry bulkheadRegistry,
            @Autowired(required = false) List<BulkheadAspectExt> bulkHeadAspectExtList,
            FallbackDecorators fallbackDecorators,
            SpelResolver spelResolver) {
        return new BulkheadAspect(prop, threadPoolBulkheadRegistry, bulkheadRegistry, bulkHeadAspectExtList, fallbackDecorators, spelResolver);
    }

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public ReactorBulkheadAspectExt reactorBulkHeadAspectExt() {
        return new ReactorBulkheadAspectExt();
    }

    @Bean
    public EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }
}
