package com.github.liyibo1110.resilience4j.bulkhead;

import com.github.liyibo1110.resilience4j.core.Registry;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import io.vavr.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-09 11:44
 */
public interface BulkheadRegistry extends Registry<Bulkhead, BulkheadConfig> {

    static BulkheadRegistry of(BulkheadConfig config) {
        return new InMemoryBulkheadRegistry(config);
    }

    static BulkheadRegistry of(BulkheadConfig config,
                               io.vavr.collection.Map<String, String> tags) {
        return new InMemoryBulkheadRegistry(config, tags);
    }

    static BulkheadRegistry of(BulkheadConfig config,
                               RegistryEventConsumer<Bulkhead> registryEventConsumer) {
        return new InMemoryBulkheadRegistry(config, registryEventConsumer);
    }

    static BulkheadRegistry of(BulkheadConfig config,
                               List<RegistryEventConsumer<Bulkhead>> registryEventConsumers) {
        return new InMemoryBulkheadRegistry(config, registryEventConsumers);
    }

    static BulkheadRegistry of(Map<String, BulkheadConfig> configs) {
        return new InMemoryBulkheadRegistry(configs);
    }

    static BulkheadRegistry of(Map<String, BulkheadConfig> configs,
                               io.vavr.collection.Map<String, String> tags) {
        return new InMemoryBulkheadRegistry(configs, tags);
    }

    static BulkheadRegistry of(Map<String, BulkheadConfig> configs,
                               RegistryEventConsumer<Bulkhead> registryEventConsumer) {
        return new InMemoryBulkheadRegistry(configs, registryEventConsumer);
    }

    static BulkheadRegistry of(Map<String, BulkheadConfig> configs,
                               RegistryEventConsumer<Bulkhead> registryEventConsumer,
                               io.vavr.collection.Map<String, String> tags) {
        return new InMemoryBulkheadRegistry(configs, registryEventConsumer, tags);
    }

    static BulkheadRegistry of(Map<String, BulkheadConfig> configs,
                               List<RegistryEventConsumer<Bulkhead>> registryEventConsumers) {
        return new InMemoryBulkheadRegistry(configs, registryEventConsumers);
    }

    static BulkheadRegistry ofDefaults() {
        return new InMemoryBulkheadRegistry(BulkheadConfig.ofDefaults());
    }

    Seq<Bulkhead> getAllBulkheads();

    Bulkhead bulkhead(String name);

    Bulkhead bulkhead(String name, io.vavr.collection.Map<String, String> tags);

    Bulkhead bulkhead(String name, BulkheadConfig config);

    Bulkhead bulkhead(String name, BulkheadConfig config,
                      io.vavr.collection.Map<String, String> tags);

    Bulkhead bulkhead(String name, Supplier<BulkheadConfig> configSupplier);

    Bulkhead bulkhead(String name, Supplier<BulkheadConfig> configSupplier,
                      io.vavr.collection.Map<String, String> tags);

    Bulkhead bulkhead(String name, String configName);

    Bulkhead bulkhead(String name, String configName, io.vavr.collection.Map<String, String> tags);

    static Builder custom() {
        return new Builder();
    }

    class Builder {
        private static final String DEFAULT_CONFIG = "default";
        private RegistryStore<Bulkhead> registryStore;
        private Map<String, BulkheadConfig> bulkheadConfigsMap;
        private List<RegistryEventConsumer<Bulkhead>> registryEventConsumers;
        private io.vavr.collection.Map<String, String> tags;

        public Builder() {
            this.bulkheadConfigsMap = new java.util.HashMap<>();
            this.registryEventConsumers = new ArrayList<>();
        }

        public Builder withRegistryStore(RegistryStore<Bulkhead> registryStore) {
            this.registryStore = registryStore;
            return this;
        }

        public Builder withBulkheadConfig(BulkheadConfig bulkheadConfig) {
            bulkheadConfigsMap.put(DEFAULT_CONFIG, bulkheadConfig);
            return this;
        }

        public Builder addBulkheadConfig(String configName, BulkheadConfig configuration) {
            if(configName.equals(DEFAULT_CONFIG))
                throw new IllegalArgumentException("You cannot add another configuration with name 'default' as it is preserved for default configuration");
            bulkheadConfigsMap.put(configName, configuration);
            return this;
        }

        public Builder addRegistryEventConsumer(RegistryEventConsumer<Bulkhead> registryEventConsumer) {
            this.registryEventConsumers.add(registryEventConsumer);
            return this;
        }

        public Builder withTags(io.vavr.collection.Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public BulkheadRegistry build() {
            return new InMemoryBulkheadRegistry(bulkheadConfigsMap, registryEventConsumers, tags, registryStore);
        }
    }
}
