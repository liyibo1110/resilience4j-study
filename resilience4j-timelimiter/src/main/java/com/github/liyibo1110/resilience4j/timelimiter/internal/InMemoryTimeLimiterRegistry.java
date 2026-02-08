package com.github.liyibo1110.resilience4j.timelimiter.internal;

import com.github.liyibo1110.resilience4j.core.ConfigurationNotFoundException;
import com.github.liyibo1110.resilience4j.core.registry.AbstractRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterConfig;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-08 19:44
 */
public class InMemoryTimeLimiterRegistry extends AbstractRegistry<TimeLimiter, TimeLimiterConfig> implements TimeLimiterRegistry {

    public InMemoryTimeLimiterRegistry() {
        this(TimeLimiterConfig.ofDefaults(), HashMap.empty());
    }

    public InMemoryTimeLimiterRegistry(io.vavr.collection.Map<String, String> tags) {
        this(TimeLimiterConfig.ofDefaults(), tags);
    }

    public InMemoryTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs) {
        this(configs.getOrDefault(DEFAULT_CONFIG, TimeLimiterConfig.ofDefaults()));
        this.configurations.putAll(configs);
    }

    public InMemoryTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs,
                                       io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, TimeLimiterConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs,
                                       RegistryEventConsumer<TimeLimiter> registryEventConsumer) {
        this(configs.getOrDefault(DEFAULT_CONFIG, TimeLimiterConfig.ofDefaults()), registryEventConsumer);
        this.configurations.putAll(configs);
    }

    public InMemoryTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs,
                                       RegistryEventConsumer<TimeLimiter> registryEventConsumer,
                                       io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, TimeLimiterConfig.ofDefaults()), registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs,
                                       List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers) {
        this(configs.getOrDefault(DEFAULT_CONFIG, TimeLimiterConfig.ofDefaults()), registryEventConsumers);
        this.configurations.putAll(configs);
    }

    public InMemoryTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs,
                                       List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers,
                                       io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, TimeLimiterConfig.ofDefaults()), registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryTimeLimiterRegistry(TimeLimiterConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryTimeLimiterRegistry(TimeLimiterConfig defaultConfig,
                                       io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryTimeLimiterRegistry(TimeLimiterConfig defaultConfig,
                                       RegistryEventConsumer<TimeLimiter> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryTimeLimiterRegistry(TimeLimiterConfig defaultConfig,
                                       RegistryEventConsumer<TimeLimiter> registryEventConsumer,
                                       io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryTimeLimiterRegistry(TimeLimiterConfig defaultConfig,
                                       List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    public InMemoryTimeLimiterRegistry(TimeLimiterConfig defaultConfig,
                                       List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers,
                                       io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    @Override
    public Seq<TimeLimiter> getAllTimeLimiters() {
        return Array.ofAll(entryMap.values());
    }

    @Override
    public TimeLimiter timeLimiter(final String name) {
        return timeLimiter(name, getDefaultConfig(), HashMap.empty());
    }

    @Override
    public TimeLimiter timeLimiter(String name, io.vavr.collection.Map<String, String> tags) {
        return timeLimiter(name, getDefaultConfig(), tags);
    }

    @Override
    public TimeLimiter timeLimiter(final String name, final TimeLimiterConfig config) {
        return timeLimiter(name, config, HashMap.empty());
    }

    @Override
    public TimeLimiter timeLimiter(String name, TimeLimiterConfig config,
                                   io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> TimeLimiter.of(name,
                Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public TimeLimiter timeLimiter(final String name, final Supplier<TimeLimiterConfig> configSupplier) {
        return timeLimiter(name, configSupplier, HashMap.empty());
    }

    @Override
    public TimeLimiter timeLimiter(String name, Supplier<TimeLimiterConfig> configSupplier,
                                   io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> TimeLimiter.of(name, Objects.requireNonNull(
                Objects.requireNonNull(configSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public TimeLimiter timeLimiter(String name, String configName) {
        return timeLimiter(name, configName, HashMap.empty());
    }

    @Override
    public TimeLimiter timeLimiter(String name, String configName,
                                   io.vavr.collection.Map<String, String> tags) {
        TimeLimiterConfig config = getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName));
        return timeLimiter(name, config, tags);
    }
}
