package com.github.liyibo1110.resilience4j.ratelimiter;

import com.github.liyibo1110.resilience4j.core.Registry;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import io.vavr.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-08 12:16
 */
public interface RateLimiterRegistry extends Registry<RateLimiter, RateLimiterConfig> {

    static RateLimiterRegistry of(RateLimiterConfig defaultConfig) {
        return new InMemoryRateLimiterRegistry(defaultConfig);
    }

    static RateLimiterRegistry of(RateLimiterConfig defaultConfig,
                                  RegistryEventConsumer<RateLimiter> registryEventConsumer) {
        return new InMemoryRateLimiterRegistry(defaultConfig, registryEventConsumer);
    }

    static RateLimiterRegistry of(RateLimiterConfig defaultConfig,
                                  List<RegistryEventConsumer<RateLimiter>> registryEventConsumers) {
        return new InMemoryRateLimiterRegistry(defaultConfig, registryEventConsumers);
    }

    static RateLimiterRegistry ofDefaults() {
        return new InMemoryRateLimiterRegistry(RateLimiterConfig.ofDefaults());
    }

    static RateLimiterRegistry of(Map<String, RateLimiterConfig> configs) {
        return new InMemoryRateLimiterRegistry(configs);
    }

    static RateLimiterRegistry of(Map<String, RateLimiterConfig> configs,
                                  io.vavr.collection.Map<String, String> tags) {
        return new InMemoryRateLimiterRegistry(configs, tags);
    }

    static RateLimiterRegistry of(Map<String, RateLimiterConfig> configs,
                                  RegistryEventConsumer<RateLimiter> registryEventConsumer) {
        return new InMemoryRateLimiterRegistry(configs, registryEventConsumer);
    }

    static RateLimiterRegistry of(Map<String, RateLimiterConfig> configs,
                                  RegistryEventConsumer<RateLimiter> registryEventConsumer,
                                  io.vavr.collection.Map<String, String> tags) {
        return new InMemoryRateLimiterRegistry(configs, registryEventConsumer, tags);
    }

    static RateLimiterRegistry of(Map<String, RateLimiterConfig> configs,
                                  List<RegistryEventConsumer<RateLimiter>> registryEventConsumers) {
        return new InMemoryRateLimiterRegistry(configs, registryEventConsumers);
    }

    Seq<RateLimiter> getAllRateLimiters();

    RateLimiter rateLimiter(String name);

    RateLimiter rateLimiter(String name, io.vavr.collection.Map<String, String> tags);

    RateLimiter rateLimiter(String name, RateLimiterConfig config);

    RateLimiter rateLimiter(String name, RateLimiterConfig config,
                            io.vavr.collection.Map<String, String> tags);

    RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> configSupplier);

    RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> configSupplier,
                            io.vavr.collection.Map<String, String> tags);

    RateLimiter rateLimiter(String name, String configName);

    RateLimiter rateLimiter(String name, String configName,
                            io.vavr.collection.Map<String, String> tags);

    static Builder custom() {
        return new Builder();
    }

    class Builder {
        private static final String DEFAULT_CONFIG = "default";
        private RegistryStore<RateLimiter> registryStore;
        private Map<String, RateLimiterConfig> configsMap;
        private List<RegistryEventConsumer<RateLimiter>> registryEventConsumers;
        private io.vavr.collection.Map<String, String> tags;

        public Builder() {
            this.configsMap = new java.util.HashMap<>();
            this.registryEventConsumers = new ArrayList<>();
        }

        public Builder withRegistryStore(RegistryStore<RateLimiter> registryStore) {
            this.registryStore = registryStore;
            return this;
        }

        public Builder withRateLimiterConfig(RateLimiterConfig config) {
            configsMap.put(DEFAULT_CONFIG, config);
            return this;
        }

        public Builder addRateLimiterConfig(String configName, RateLimiterConfig config) {
            if(configName.equals(DEFAULT_CONFIG))
                throw new IllegalArgumentException("You cannot add another configuration with name 'default' as it is preserved for default configuration");
            configsMap.put(configName, config);
            return this;
        }

        public Builder addRegistryEventConsumer(RegistryEventConsumer<RateLimiter> registryEventConsumer) {
            this.registryEventConsumers.add(registryEventConsumer);
            return this;
        }

        public Builder withTags(io.vavr.collection.Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public RateLimiterRegistry build() {
            return new InMemoryRateLimiterRegistry(configsMap, registryEventConsumers, tags,
                    registryStore);
        }
    }
}
