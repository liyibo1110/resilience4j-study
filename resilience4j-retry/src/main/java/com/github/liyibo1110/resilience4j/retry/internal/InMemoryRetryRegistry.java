package com.github.liyibo1110.resilience4j.retry.internal;

import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.AbstractRegistry;
import com.github.liyibo1110.resilience4j.core.registry.InMemoryRegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryConfig;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-06 12:05
 */
public final class InMemoryRetryRegistry extends AbstractRegistry<Retry, RetryConfig>
                implements RetryRegistry {

    public InMemoryRetryRegistry() {
        this(RetryConfig.ofDefaults());
    }

    public InMemoryRetryRegistry(io.vavr.collection.Map<String, String> tags) {
        this(RetryConfig.ofDefaults(), tags);
    }

    public InMemoryRetryRegistry(java.util.Map<String, RetryConfig> configs) {
        this(configs, HashMap.empty());
    }

    public InMemoryRetryRegistry(java.util.Map<String, RetryConfig> configs,
                                 io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RetryConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRetryRegistry(java.util.Map<String, RetryConfig> configs,
                                 RegistryEventConsumer<Retry> registryEventConsumer) {
        this(configs, registryEventConsumer, HashMap.empty());
    }

    public InMemoryRetryRegistry(java.util.Map<String, RetryConfig> configs,
                                 RegistryEventConsumer<Retry> registryEventConsumer,
                                 io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RetryConfig.ofDefaults()), registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRetryRegistry(java.util.Map<String, RetryConfig> configs,
                                 List<RegistryEventConsumer<Retry>> registryEventConsumers) {
        this(configs, registryEventConsumers, HashMap.empty());
    }

    public InMemoryRetryRegistry(java.util.Map<String, RetryConfig> configs,
                                 List<RegistryEventConsumer<Retry>> registryEventConsumers,
                                 io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RetryConfig.ofDefaults()),
                registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRetryRegistry(java.util.Map<String, RetryConfig> configs,
                                 List<RegistryEventConsumer<Retry>> registryEventConsumers,
                                 io.vavr.collection.Map<String, String> tags, RegistryStore<Retry> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, RetryConfig.ofDefaults()),
                registryEventConsumers, Optional.ofNullable(tags).orElse(HashMap.empty()),
                Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }

    public InMemoryRetryRegistry(RetryConfig defaultConfig) {
        this(defaultConfig, HashMap.empty());
    }

    public InMemoryRetryRegistry(RetryConfig defaultConfig,
                                 io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryRetryRegistry(RetryConfig defaultConfig,
                                 RegistryEventConsumer<Retry> registryEventConsumer) {
        this(defaultConfig, registryEventConsumer, HashMap.empty());
    }

    public InMemoryRetryRegistry(RetryConfig defaultConfig,
                                 RegistryEventConsumer<Retry> registryEventConsumer,
                                 io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryRetryRegistry(RetryConfig defaultConfig,
                                 List<RegistryEventConsumer<Retry>> registryEventConsumers) {
        this(defaultConfig, registryEventConsumers, HashMap.empty());
    }

    public InMemoryRetryRegistry(RetryConfig defaultConfig,
                                 List<RegistryEventConsumer<Retry>> registryEventConsumers,
                                 io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    @Override
    public Seq<Retry> getAllRetries() {
        return Array.ofAll(entryMap.values());
    }

    @Override
    public Retry retry(String name) {
        return retry(name, getDefaultConfig());
    }

    @Override
    public Retry retry(String name, Map<String, String> tags) {
        return retry(name, getDefaultConfig(), tags);
    }

    @Override
    public Retry retry(String name, RetryConfig config) {
        return retry(name, config, HashMap.empty());
    }

    @Override
    public Retry retry(String name, RetryConfig config, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Retry
                .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public Retry retry(String name, Supplier<RetryConfig> configSupplier) {
        return retry(name, configSupplier, HashMap.empty());
    }

    @Override
    public Retry retry(String name, Supplier<RetryConfig> configSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Retry.of(name, Objects.requireNonNull(
                Objects.requireNonNull(configSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public Retry retry(String name, String configName) {
        return retry(name, configName, HashMap.empty());
    }

    @Override
    public Retry retry(String name, String configName, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Retry.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }
}
