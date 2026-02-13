package com.github.liyibo1110.resilience4j.bulkhead.configure.threadpool;

import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigurationProperties;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.CompositeRegistryEventConsumer;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-13 11:46
 */
@Configuration
public class ThreadPoolBulkheadConfiguration {

    @Bean
    @Qualifier("compositeThreadPoolBulkheadCustomizer")
    public CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer(
            @Autowired(required = false) List<ThreadPoolBulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry(
            ThreadPoolBulkheadConfigurationProperties bulkheadConfigurationProperties,
            EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
            RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
            @Qualifier("compositeThreadPoolBulkheadCustomizer") CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
        ThreadPoolBulkheadRegistry bulkheadRegistry = createBulkheadRegistry(
                bulkheadConfigurationProperties, threadPoolBulkheadRegistryEventConsumer,
                compositeThreadPoolBulkheadCustomizer);
        registerEventConsumer(bulkheadRegistry, bulkheadEventConsumerRegistry,
                bulkheadConfigurationProperties);
        bulkheadConfigurationProperties.getBackends().forEach((name, properties) -> bulkheadRegistry
                .bulkhead(name, bulkheadConfigurationProperties
                        .createThreadPoolBulkheadConfig(name, compositeThreadPoolBulkheadCustomizer)));
        return bulkheadRegistry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<ThreadPoolBulkhead>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    private ThreadPoolBulkheadRegistry createBulkheadRegistry(
            ThreadPoolBulkheadConfigurationProperties threadPoolBulkheadConfigurationProperties,
            RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
            CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
        Map<String, ThreadPoolBulkheadConfig> configs = threadPoolBulkheadConfigurationProperties
                .getConfigs()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> threadPoolBulkheadConfigurationProperties
                                .createThreadPoolBulkheadConfig(entry.getValue(),
                                        compositeThreadPoolBulkheadCustomizer, entry.getKey())));
        return ThreadPoolBulkheadRegistry.of(configs, threadPoolBulkheadRegistryEventConsumer,
                io.vavr.collection.HashMap.ofAll(threadPoolBulkheadConfigurationProperties.getTags()));
    }

    private void registerEventConsumer(ThreadPoolBulkheadRegistry bulkheadRegistry,
                                       EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                       ThreadPoolBulkheadConfigurationProperties properties) {
        bulkheadRegistry.getEventPublisher()
                .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
                .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
    }

    private void registerEventConsumer(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                       ThreadPoolBulkhead bulkHead,
                                       ThreadPoolBulkheadConfigurationProperties bulkheadConfigurationProperties) {
        int eventConsumerBufferSize = Optional.ofNullable(bulkheadConfigurationProperties.getBackendProperties(bulkHead.getName()))
                .map(ThreadPoolBulkheadConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                .orElse(100);
        bulkHead.getEventPublisher().onEvent(eventConsumerRegistry.createEventConsumer(
                String.join("-", ThreadPoolBulkhead.class.getSimpleName(), bulkHead.getName()),
                eventConsumerBufferSize));
    }
}
