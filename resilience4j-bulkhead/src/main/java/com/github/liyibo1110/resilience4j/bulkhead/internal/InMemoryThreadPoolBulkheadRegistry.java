package com.github.liyibo1110.resilience4j.bulkhead.internal;

import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
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
 * @date 2026-02-09 13:42
 */
public final class InMemoryThreadPoolBulkheadRegistry extends AbstractRegistry<ThreadPoolBulkhead, ThreadPoolBulkheadConfig>
                implements ThreadPoolBulkheadRegistry {

    public InMemoryThreadPoolBulkheadRegistry() {
        this(HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(io.vavr.collection.Map<String, String> tags) {
        this(ThreadPoolBulkheadConfig.ofDefaults(), tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs) {
        this(configs, HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs,
                                              io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs,
                                              RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        this(configs, registryEventConsumer, HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs, RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer,
                                              io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()), registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs, List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        this(configs, registryEventConsumers, HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs, List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
                                              io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()), registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig, io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig, RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig, RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer,
                                              io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig, List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig, List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
                                              io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs,
                                              List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
                                              io.vavr.collection.Map<String, String> tags, RegistryStore<ThreadPoolBulkhead> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()),
                registryEventConsumers, Optional.ofNullable(tags).orElse(HashMap.empty()),
                Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }

    @Override
    public Seq<ThreadPoolBulkhead> getAllBulkheads() {
        return Array.ofAll(entryMap.values());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name) {
        return bulkhead(name, HashMap.empty());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, io.vavr.collection.Map<String, String> tags) {
        return bulkhead(name, getDefaultConfig(), tags);
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config) {
        return bulkhead(name, config, HashMap.empty());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config,
                                       io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead
                .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, Supplier<ThreadPoolBulkheadConfig> configSupplier) {
        return bulkhead(name, configSupplier, HashMap.empty());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, Supplier<ThreadPoolBulkheadConfig> configSupplier,
                                       io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead.of(name, Objects.requireNonNull(
                Objects.requireNonNull(configSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName) {
        return bulkhead(name, configName, HashMap.empty());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName,
                                       io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }

    @Override
    public void close() throws Exception {
        for(ThreadPoolBulkhead bulkhead : this.getAllBulkheads())
            bulkhead.close();
    }
}
