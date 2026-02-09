package com.github.liyibo1110.resilience4j.bulkhead.internal;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadConfig;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
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
 * @author liyibo
 * @date 2026-02-09 12:08
 */
public final class InMemoryBulkheadRegistry extends AbstractRegistry<Bulkhead, BulkheadConfig>
            implements BulkheadRegistry {

    public InMemoryBulkheadRegistry() {
        this(BulkheadConfig.ofDefaults());
    }

    public InMemoryBulkheadRegistry(io.vavr.collection.Map<String, String> tags) {
        this(BulkheadConfig.ofDefaults(), tags);
    }

    public InMemoryBulkheadRegistry(Map<String, BulkheadConfig> configs) {
        this(configs, HashMap.empty());
    }

    public InMemoryBulkheadRegistry(Map<String, BulkheadConfig> configs,
                                    io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, BulkheadConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryBulkheadRegistry(Map<String, BulkheadConfig> configs, RegistryEventConsumer<Bulkhead> registryEventConsumer) {
        this(configs, registryEventConsumer, HashMap.empty());
    }

    public InMemoryBulkheadRegistry(Map<String, BulkheadConfig> configs, RegistryEventConsumer<Bulkhead> registryEventConsumer,
            io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, BulkheadConfig.ofDefaults()), registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryBulkheadRegistry(Map<String, BulkheadConfig> configs, List<RegistryEventConsumer<Bulkhead>> registryEventConsumers) {
        this(configs, registryEventConsumers, HashMap.empty());
    }

    public InMemoryBulkheadRegistry(Map<String, BulkheadConfig> configs, List<RegistryEventConsumer<Bulkhead>> registryEventConsumers,
            io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, BulkheadConfig.ofDefaults()), registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryBulkheadRegistry(BulkheadConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryBulkheadRegistry(BulkheadConfig defaultConfig, io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryBulkheadRegistry(BulkheadConfig defaultConfig, List<RegistryEventConsumer<Bulkhead>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    public InMemoryBulkheadRegistry(BulkheadConfig defaultConfig, List<RegistryEventConsumer<Bulkhead>> registryEventConsumers,
                                    io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    public InMemoryBulkheadRegistry(BulkheadConfig defaultConfig, RegistryEventConsumer<Bulkhead> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryBulkheadRegistry(BulkheadConfig defaultConfig, RegistryEventConsumer<Bulkhead> registryEventConsumer,
                                    io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryBulkheadRegistry(Map<String, BulkheadConfig> configs, List<RegistryEventConsumer<Bulkhead>> registryEventConsumers,
                                    io.vavr.collection.Map<String, String> tags, RegistryStore<Bulkhead> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, BulkheadConfig.ofDefaults()),
                registryEventConsumers, Optional.ofNullable(tags).orElse(HashMap.empty()),
                Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }

    @Override
    public Seq<Bulkhead> getAllBulkheads() {
        return Array.ofAll(entryMap.values());
    }

    @Override
    public Bulkhead bulkhead(String name) {
        return bulkhead(name, HashMap.empty());
    }

    @Override
    public Bulkhead bulkhead(String name, io.vavr.collection.Map<String, String> tags) {
        return bulkhead(name, getDefaultConfig(), getAllTags(tags));
    }

    @Override
    public Bulkhead bulkhead(String name, BulkheadConfig config) {
        return bulkhead(name, config, HashMap.empty());
    }

    @Override
    public Bulkhead bulkhead(String name, BulkheadConfig config,
                             io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> Bulkhead.of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public Bulkhead bulkhead(String name, Supplier<BulkheadConfig> configSupplier) {
        return bulkhead(name, configSupplier, HashMap.empty());
    }

    @Override
    public Bulkhead bulkhead(String name, Supplier<BulkheadConfig> configSupplier,
                             io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> Bulkhead.of(name, Objects.requireNonNull(
                Objects.requireNonNull(configSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public Bulkhead bulkhead(String name, String configName) {
        return bulkhead(name, configName, HashMap.empty());
    }

    @Override
    public Bulkhead bulkhead(String name, String configName,
                             io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> Bulkhead.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }
}
