package com.github.liyibo1110.resilience4j.circuitbreaker.internal;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.AbstractRegistry;
import com.github.liyibo1110.resilience4j.core.registry.InMemoryRegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * CircuitBreakerRegistry的实现（基于AbstractRegistry内部的ConcurrentHashMap）
 * @author liyibo
 * @date 2026-02-05 16:23
 */
public final class InMemoryCircuitBreakerRegistry extends AbstractRegistry<CircuitBreaker, CircuitBreakerConfig> implements CircuitBreakerRegistry {
    public InMemoryCircuitBreakerRegistry() {
        this(HashMap.empty());
    }

    public InMemoryCircuitBreakerRegistry(io.vavr.collection.Map<String, String> tags) {
        this(CircuitBreakerConfig.ofDefaults(), tags);
    }

    public InMemoryCircuitBreakerRegistry(Map<String, CircuitBreakerConfig> configs) {
        this(configs, HashMap.empty());
    }

    public InMemoryCircuitBreakerRegistry(Map<String, CircuitBreakerConfig> configs, io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, CircuitBreakerConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryCircuitBreakerRegistry(Map<String, CircuitBreakerConfig> configs,
                                          RegistryEventConsumer<CircuitBreaker> registryEventConsumer) {
        this(configs.getOrDefault(DEFAULT_CONFIG, CircuitBreakerConfig.ofDefaults()), registryEventConsumer);
        this.configurations.putAll(configs);
    }

    public InMemoryCircuitBreakerRegistry(Map<String, CircuitBreakerConfig> configs, RegistryEventConsumer<CircuitBreaker> registryEventConsumer,
                                          io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, CircuitBreakerConfig.ofDefaults()), registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryCircuitBreakerRegistry(Map<String, CircuitBreakerConfig> configs,
                                          List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers,
                                          io.vavr.collection.Map<String, String> tags, RegistryStore<CircuitBreaker> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, CircuitBreakerConfig.ofDefaults()),
                registryEventConsumers, Optional.ofNullable(tags).orElse(HashMap.empty()),
                Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }

    public InMemoryCircuitBreakerRegistry(Map<String, CircuitBreakerConfig> configs,
                                          List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers) {
        this(configs.getOrDefault(DEFAULT_CONFIG, CircuitBreakerConfig.ofDefaults()), registryEventConsumers);
        this.configurations.putAll(configs);
    }

    public InMemoryCircuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryCircuitBreakerRegistry(CircuitBreakerConfig defaultConfig, io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryCircuitBreakerRegistry(CircuitBreakerConfig defaultConfig, RegistryEventConsumer<CircuitBreaker> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryCircuitBreakerRegistry(CircuitBreakerConfig defaultConfig, RegistryEventConsumer<CircuitBreaker> registryEventConsumer,
                                          io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryCircuitBreakerRegistry(CircuitBreakerConfig defaultConfig, List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    @Override
    public Seq<CircuitBreaker> getAllCircuitBreakers() {
        return Array.ofAll(entryMap.values());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name) {
        return circuitBreaker(name, getDefaultConfig());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, io.vavr.collection.Map<String, String> tags) {
        return circuitBreaker(name, getDefaultConfig(), tags);
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig config) {
        return circuitBreaker(name, config, HashMap.empty());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig config, io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> CircuitBreaker.of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, String configName) {
        return circuitBreaker(name, configName, HashMap.empty());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, String configName,
                                         io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> CircuitBreaker.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, Supplier<CircuitBreakerConfig> configSupplier) {
        return circuitBreaker(name, configSupplier, HashMap.empty());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, Supplier<CircuitBreakerConfig> configSupplier,
                                         io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> CircuitBreaker.of(name, Objects.requireNonNull(
                Objects.requireNonNull(configSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }
}
