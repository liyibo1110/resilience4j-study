package com.github.liyibo1110.resilience4j.circuitbreaker;

import com.github.liyibo1110.resilience4j.circuitbreaker.internal.InMemoryCircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.core.Registry;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * CircuitBreaker -> CircuitBreakerConfig的映射关系存储
 * @author liyibo
 * @date 2026-02-05 16:22
 */
public interface CircuitBreakerRegistry extends Registry<CircuitBreaker, CircuitBreakerConfig> {
    static CircuitBreakerRegistry of(CircuitBreakerConfig config) {
        return new InMemoryCircuitBreakerRegistry(config);
    }

    static CircuitBreakerRegistry of(CircuitBreakerConfig config, RegistryEventConsumer<CircuitBreaker> registryEventConsumer) {
        return new InMemoryCircuitBreakerRegistry(config, registryEventConsumer);
    }

    static CircuitBreakerRegistry of(CircuitBreakerConfig config,
                                     List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers) {
        return new InMemoryCircuitBreakerRegistry(config, registryEventConsumers);
    }

    static CircuitBreakerRegistry of(Map<String, CircuitBreakerConfig> configs) {
        return of(configs, HashMap.empty());
    }

    static CircuitBreakerRegistry of(Map<String, CircuitBreakerConfig> configs, io.vavr.collection.Map<String, String> tags) {
        return new InMemoryCircuitBreakerRegistry(configs, tags);
    }

    static CircuitBreakerRegistry of(Map<String, CircuitBreakerConfig> configs, RegistryEventConsumer<CircuitBreaker> registryEventConsumer) {
        return new InMemoryCircuitBreakerRegistry(configs, registryEventConsumer);
    }

    static CircuitBreakerRegistry of(Map<String, CircuitBreakerConfig> configs, RegistryEventConsumer<CircuitBreaker> registryEventConsumer,
                                     io.vavr.collection.Map<String, String> tags) {
        return new InMemoryCircuitBreakerRegistry(configs, registryEventConsumer, tags);
    }

    static CircuitBreakerRegistry of(Map<String, CircuitBreakerConfig> configs, List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers) {
        return new InMemoryCircuitBreakerRegistry(configs, registryEventConsumers);
    }

    static CircuitBreakerRegistry ofDefaults() {
        return new InMemoryCircuitBreakerRegistry();
    }

    Seq<CircuitBreaker> getAllCircuitBreakers();

    CircuitBreaker circuitBreaker(String name);

    CircuitBreaker circuitBreaker(String name, io.vavr.collection.Map<String, String> tags);

    CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig config);

    CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig config, io.vavr.collection.Map<String, String> tags);

    CircuitBreaker circuitBreaker(String name, String configName);

    CircuitBreaker circuitBreaker(String name, String configName, io.vavr.collection.Map<String, String> tags);

    CircuitBreaker circuitBreaker(String name, Supplier<CircuitBreakerConfig> configSupplier);

    CircuitBreaker circuitBreaker(String name, Supplier<CircuitBreakerConfig> configSupplier,
                                  io.vavr.collection.Map<String, String> tags);

    static Builder custom() {
        return new Builder();
    }

    class Builder {
        private static final String DEFAULT_CONFIG = "default";
        private RegistryStore<CircuitBreaker> registryStore;
        private Map<String, CircuitBreakerConfig> circuitBreakerConfigsMap;
        private List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers;
        private io.vavr.collection.Map<String, String> tags;

        public Builder() {
            this.circuitBreakerConfigsMap = new java.util.HashMap<>();
            this.registryEventConsumers = new ArrayList<>();
        }

        public Builder withRegistryStore(RegistryStore<CircuitBreaker> registryStore) {
            this.registryStore = registryStore;
            return this;
        }

        public Builder withCircuitBreakerConfig(CircuitBreakerConfig config) {
            circuitBreakerConfigsMap.put(DEFAULT_CONFIG, config);
            return this;
        }

        public Builder addCircuitBreakerConfig(String configName, CircuitBreakerConfig configuration) {
            if(configName.equals(DEFAULT_CONFIG))
                throw new IllegalArgumentException("You cannot add another configuration with name 'default' as it is preserved for default configuration");
            circuitBreakerConfigsMap.put(configName, configuration);
            return this;
        }

        public Builder addRegistryEventConsumer(RegistryEventConsumer<CircuitBreaker> registryEventConsumer) {
            this.registryEventConsumers.add(registryEventConsumer);
            return this;
        }

        public Builder withTags(io.vavr.collection.Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public CircuitBreakerRegistry build() {
            return new InMemoryCircuitBreakerRegistry(circuitBreakerConfigsMap, registryEventConsumers, tags, registryStore);
        }
    }
}
