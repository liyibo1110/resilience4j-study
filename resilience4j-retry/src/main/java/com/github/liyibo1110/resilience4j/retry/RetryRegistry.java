package com.github.liyibo1110.resilience4j.retry;

import com.github.liyibo1110.resilience4j.core.Registry;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.retry.internal.InMemoryRetryRegistry;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-06 12:00
 */
public interface RetryRegistry extends Registry<Retry, RetryConfig> {

    static RetryRegistry of(RetryConfig retryConfig) {
        return new InMemoryRetryRegistry(retryConfig);
    }

    static RetryRegistry of(RetryConfig retryConfig, RegistryEventConsumer<Retry> registryEventConsumer) {
        return new InMemoryRetryRegistry(retryConfig, registryEventConsumer);
    }

    static RetryRegistry of(RetryConfig retryConfig, List<RegistryEventConsumer<Retry>> registryEventConsumers) {
        return new InMemoryRetryRegistry(retryConfig, registryEventConsumers);
    }

    static RetryRegistry ofDefaults() {
        return new InMemoryRetryRegistry();
    }

    static RetryRegistry of(Map<String, RetryConfig> configs) {
        return of(configs, HashMap.empty());
    }

    static RetryRegistry of(Map<String, RetryConfig> configs,
                            io.vavr.collection.Map<String, String> tags) {
        return new InMemoryRetryRegistry(configs, tags);
    }

    static RetryRegistry of(Map<String, RetryConfig> configs,
                            RegistryEventConsumer<Retry> registryEventConsumer) {
        return new InMemoryRetryRegistry(configs, registryEventConsumer);
    }

    static RetryRegistry of(Map<String, RetryConfig> configs,
                            RegistryEventConsumer<Retry> registryEventConsumer,
                            io.vavr.collection.Map<String, String> tags) {
        return new InMemoryRetryRegistry(configs, registryEventConsumer, tags);
    }

    static RetryRegistry of(Map<String, RetryConfig> configs,
                            List<RegistryEventConsumer<Retry>> registryEventConsumers) {
        return new InMemoryRetryRegistry(configs, registryEventConsumers);
    }

    Seq<Retry> getAllRetries();
    Retry retry(String name);
    Retry retry(String name, io.vavr.collection.Map<String, String> tags);
    Retry retry(String name, RetryConfig config);
    Retry retry(String name, RetryConfig config, io.vavr.collection.Map<String, String> tags);
    Retry retry(String name, Supplier<RetryConfig> configSupplier);
    Retry retry(String name, Supplier<RetryConfig> configSupplier, io.vavr.collection.Map<String, String> tags);
    Retry retry(String name, String configName);
    Retry retry(String name, String configName, io.vavr.collection.Map<String, String> tags);

    static Builder custom() {
        return new Builder();
    }

    class Builder {
        private static final String DEFAULT_CONFIG = "default";
        private RegistryStore<Retry> registryStore;
        private Map<String, RetryConfig> retryConfigsMap;
        private List<RegistryEventConsumer<Retry>> registryEventConsumers;
        private io.vavr.collection.Map<String, String> tags;

        public Builder() {
            this.retryConfigsMap = new java.util.HashMap<>();
            this.registryEventConsumers = new ArrayList<>();
        }

        public Builder withRegistryStore(RegistryStore<Retry> registryStore) {
            this.registryStore = registryStore;
            return this;
        }

        public Builder withRetryConfig(RetryConfig retryConfig) {
            retryConfigsMap.put(DEFAULT_CONFIG, retryConfig);
            return this;
        }

        public Builder addRetryConfig(String configName, RetryConfig configuration) {
            if(configName.equals(DEFAULT_CONFIG))
                throw new IllegalArgumentException("You cannot add another configuration with name 'default' as it is preserved for default configuration");
            retryConfigsMap.put(configName, configuration);
            return this;
        }

        public Builder addRegistryEventConsumer(RegistryEventConsumer<Retry> registryEventConsumer) {
            this.registryEventConsumers.add(registryEventConsumer);
            return this;
        }

        public Builder withTags(io.vavr.collection.Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public RetryRegistry build() {
            return new InMemoryRetryRegistry(retryConfigsMap, registryEventConsumers, tags, registryStore);
        }
    }
}
