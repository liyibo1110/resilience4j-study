package com.github.liyibo1110.resilience4j.ratelimiter.internal;

import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.AbstractRegistry;
import com.github.liyibo1110.resilience4j.core.registry.InMemoryRegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
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
 * @date 2026-02-08 12:23
 */
public class InMemoryRateLimiterRegistry extends AbstractRegistry<RateLimiter, RateLimiterConfig> implements RateLimiterRegistry {

    public InMemoryRateLimiterRegistry() {
        this(RateLimiterConfig.ofDefaults());
    }

    public InMemoryRateLimiterRegistry(io.vavr.collection.Map<String, String> tags) {
        this(RateLimiterConfig.ofDefaults(), tags);
    }

    public InMemoryRateLimiterRegistry(java.util.Map<String, RateLimiterConfig> configs,
                                       io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRateLimiterRegistry(java.util.Map<String, RateLimiterConfig> configs,
                                       RegistryEventConsumer<RateLimiter> registryEventConsumer) {
        this(configs, registryEventConsumer, HashMap.empty());
    }

    public InMemoryRateLimiterRegistry(java.util.Map<String, RateLimiterConfig> configs,
                                       RegistryEventConsumer<RateLimiter> registryEventConsumer,
                                       io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()),
                registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRateLimiterRegistry(java.util.Map<String, RateLimiterConfig> configs) {
        this(configs, HashMap.empty());
    }

    public InMemoryRateLimiterRegistry(java.util.Map<String, RateLimiterConfig> configs,
                                       List<RegistryEventConsumer<RateLimiter>> registryEventConsumers) {
        this(configs, registryEventConsumers, HashMap.empty());
    }

    public InMemoryRateLimiterRegistry(java.util.Map<String, RateLimiterConfig> configs,
                                       List<RegistryEventConsumer<RateLimiter>> registryEventConsumers,
                                       io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()),
                registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig,
                                       io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig,
                                       RegistryEventConsumer<RateLimiter> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig,
                                       RegistryEventConsumer<RateLimiter> registryEventConsumer,
                                       io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig,
                                       List<RegistryEventConsumer<RateLimiter>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig,
                                       List<RegistryEventConsumer<RateLimiter>> registryEventConsumers,
                                       io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    public InMemoryRateLimiterRegistry(java.util.Map<String, RateLimiterConfig> configs,
                                       List<RegistryEventConsumer<RateLimiter>> registryEventConsumers,
                                       io.vavr.collection.Map<String, String> tags, RegistryStore<RateLimiter> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()),
                registryEventConsumers, Optional.ofNullable(tags).orElse(HashMap.empty()),
                Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }

    @Override
    public Seq<RateLimiter> getAllRateLimiters() {
        return Array.ofAll(entryMap.values());
    }

    @Override
    public RateLimiter rateLimiter(String name) {
        return rateLimiter(name, getDefaultConfig());
    }

    @Override
    public RateLimiter rateLimiter(String name, Map<String, String> tags) {
        return rateLimiter(name, getDefaultConfig(), tags);
    }

    @Override
    public RateLimiter rateLimiter(String name, RateLimiterConfig config) {
        return rateLimiter(name, config, HashMap.empty());
    }

    @Override
    public RateLimiter rateLimiter(String name, RateLimiterConfig config, Map<String, String> tags) {
        return computeIfAbsent(name, () -> new AtomicRateLimiter(name,
                Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> configSupplier) {
        return rateLimiter(name, configSupplier, HashMap.empty());
    }

    @Override
    public RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> configSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> new AtomicRateLimiter(name, Objects.requireNonNull(
                Objects.requireNonNull(configSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public RateLimiter rateLimiter(String name, String configName) {
        return rateLimiter(name, configName, HashMap.empty());
    }

    @Override
    public RateLimiter rateLimiter(String name, String configName, Map<String, String> tags) {
        return computeIfAbsent(name, () -> RateLimiter.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }
}
