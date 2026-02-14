package com.github.liyibo1110.resilience4j.bulkhead;

import com.github.liyibo1110.resilience4j.bulkhead.internal.InMemoryThreadPoolBulkheadRegistry;
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
 * @author liyibo
 * @date 2026-02-09 11:48
 */
public interface ThreadPoolBulkheadRegistry extends Registry<ThreadPoolBulkhead, ThreadPoolBulkheadConfig>, AutoCloseable {

    static ThreadPoolBulkheadRegistry of(ThreadPoolBulkheadConfig config) {
        return new InMemoryThreadPoolBulkheadRegistry(config);
    }

    static ThreadPoolBulkheadRegistry of(ThreadPoolBulkheadConfig config,
                                         RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        return new InMemoryThreadPoolBulkheadRegistry(config, registryEventConsumer);
    }

    static ThreadPoolBulkheadRegistry of(ThreadPoolBulkheadConfig config,
                                         List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        return new InMemoryThreadPoolBulkheadRegistry(config, registryEventConsumers);
    }

    static ThreadPoolBulkheadRegistry ofDefaults() {
        return ofDefaults(HashMap.empty());
    }

    static ThreadPoolBulkheadRegistry ofDefaults(io.vavr.collection.Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig.ofDefaults(), tags);
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs) {
        return of(configs, HashMap.empty());
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         io.vavr.collection.Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(configs, tags);
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        return of(configs, registryEventConsumer, HashMap.empty());
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer,
                                         io.vavr.collection.Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(configs, registryEventConsumer, tags);
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        return of(configs, registryEventConsumers, HashMap.empty());
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
                                         io.vavr.collection.Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(configs, registryEventConsumers, tags);
    }

    Seq<ThreadPoolBulkhead> getAllBulkheads();

    ThreadPoolBulkhead bulkhead(String name);

    ThreadPoolBulkhead bulkhead(String name, io.vavr.collection.Map<String, String> tags);

    ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config);

    ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config,
                                io.vavr.collection.Map<String, String> tags);

    ThreadPoolBulkhead bulkhead(String name,
                                Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier);

    ThreadPoolBulkhead bulkhead(String name,
                                Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier,
                                io.vavr.collection.Map<String, String> tags);

    ThreadPoolBulkhead bulkhead(String name, String configName);

    ThreadPoolBulkhead bulkhead(String name, String configName,
                                io.vavr.collection.Map<String, String> tags);

    static Builder custom() {
        return new Builder();
    }

    class Builder {
        private static final String DEFAULT_CONFIG = "default";
        private RegistryStore<ThreadPoolBulkhead> registryStore;
        private Map<String, ThreadPoolBulkheadConfig> threadPoolBulkheadConfigsMap;
        private List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers;
        private io.vavr.collection.Map<String, String> tags;

        public Builder() {
            this.threadPoolBulkheadConfigsMap = new java.util.HashMap<>();
            this.registryEventConsumers = new ArrayList<>();
        }

        public Builder withRegistryStore(RegistryStore<ThreadPoolBulkhead> registryStore) {
            this.registryStore = registryStore;
            return this;
        }

        public Builder withThreadPoolBulkheadConfig(ThreadPoolBulkheadConfig threadPoolBulkheadConfig) {
            threadPoolBulkheadConfigsMap.put(DEFAULT_CONFIG, threadPoolBulkheadConfig);
            return this;
        }

        public Builder addThreadPoolBulkheadConfig(String configName, ThreadPoolBulkheadConfig configuration) {
            if(configName.equals(DEFAULT_CONFIG))
                throw new IllegalArgumentException("You cannot add another configuration with name 'default' as it is preserved for default configuration");
            threadPoolBulkheadConfigsMap.put(configName, configuration);
            return this;
        }

        public Builder addRegistryEventConsumer(RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
            this.registryEventConsumers.add(registryEventConsumer);
            return this;
        }

        public Builder withTags(io.vavr.collection.Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public ThreadPoolBulkheadRegistry build() {
            return new InMemoryThreadPoolBulkheadRegistry(threadPoolBulkheadConfigsMap, registryEventConsumers, tags, registryStore);
        }
    }
}
