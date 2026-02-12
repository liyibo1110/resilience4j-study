package com.github.liyibo1110.resilience4j.circuitbreaker.configure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.DefaultEventConsumerRegistry;
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
 * @date 2026-02-11 11:08
 */
@Configuration
public class CircuitBreakerConfiguration {
    private final CircuitBreakerConfigurationProperties prop;

    public CircuitBreakerConfiguration(CircuitBreakerConfigurationProperties prop) {
        this.prop = prop;
    }

    @Bean
    @Qualifier("compositeCircuitBreakerCustomizer")
    public CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer(
            @Autowired(required = false) List<CircuitBreakerConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
                                                         RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
                                                         @Qualifier("compositeCircuitBreakerCustomizer") CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer) {
        // 构建主体
        CircuitBreakerRegistry registry = this.createCircuitBreakerRegistry(prop, circuitBreakerRegistryEventConsumer, compositeCircuitBreakerCustomizer);
        // 挂接EventConsumer
        registerEventConsumer(registry, eventConsumerRegistry);
        // 创建cb组件实例
        initCircuitBreakerRegistry(registry, compositeCircuitBreakerCustomizer);
        return registry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<CircuitBreaker>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    @Bean
    public EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }

    CircuitBreakerRegistry createCircuitBreakerRegistry(
            CircuitBreakerConfigurationProperties circuitBreakerProperties,
            RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
            CompositeCustomizer<CircuitBreakerConfigCustomizer> customizerMap) {
        Map<String, CircuitBreakerConfig> configs = circuitBreakerProperties.getConfigs()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> prop.createCircuitBreakerConfig(entry.getKey(), entry.getValue(), customizerMap)));
        return CircuitBreakerRegistry.of(configs, circuitBreakerRegistryEventConsumer,
                io.vavr.collection.HashMap.ofAll(circuitBreakerProperties.getTags()));
    }

    void initCircuitBreakerRegistry(CircuitBreakerRegistry registry,
                                    CompositeCustomizer<CircuitBreakerConfigCustomizer> customizerMap) {
        // 这里创建实际的cb实例了
        prop.getInstances().forEach((name, p) -> registry.circuitBreaker(name, prop.createCircuitBreakerConfig(name, p, customizerMap)));
    }

    /**
     * 给通用的EventPublisher增加added和replaced的consumer
     */
    public void registerEventConsumer(CircuitBreakerRegistry registry, EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry) {
        registry.getEventPublisher()
                .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry()))
                .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry()));
    }

    private void registerEventConsumer(EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
                                       CircuitBreaker cb) {
        int eventConsumerBufferSize = prop.findCircuitBreakerProperties(cb.getName())
                .map(com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                .orElse(100);
        cb.getEventPublisher().onEvent(eventConsumerRegistry.createEventConsumer(cb.getName(), eventConsumerBufferSize));
    }
}
