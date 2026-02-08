package com.github.liyibo1110.resilience4j.timelimiter;

import com.github.liyibo1110.resilience4j.core.Registry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.timelimiter.internal.InMemoryTimeLimiterRegistry;
import io.vavr.collection.Seq;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author liyibo
 * @date 2026-02-08 19:41
 */
public interface TimeLimiterRegistry extends Registry<TimeLimiter, TimeLimiterConfig> {

    static TimeLimiterRegistry of(TimeLimiterConfig defaultConfig) {
        return new InMemoryTimeLimiterRegistry(defaultConfig);
    }

    static TimeLimiterRegistry of(TimeLimiterConfig defaultConfig,
                                  RegistryEventConsumer<TimeLimiter> registryEventConsumer) {
        return new InMemoryTimeLimiterRegistry(defaultConfig, registryEventConsumer);
    }

    static TimeLimiterRegistry of(TimeLimiterConfig defaultConfig,
                                  List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers) {
        return new InMemoryTimeLimiterRegistry(defaultConfig, registryEventConsumers);
    }

    static TimeLimiterRegistry ofDefaults() {
        return new InMemoryTimeLimiterRegistry(TimeLimiterConfig.ofDefaults());
    }

    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs) {
        return new InMemoryTimeLimiterRegistry(configs);
    }

    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
                                  io.vavr.collection.Map<String, String> tags) {
        return new InMemoryTimeLimiterRegistry(configs, tags);
    }

    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
                                  RegistryEventConsumer<TimeLimiter> registryEventConsumer) {
        return new InMemoryTimeLimiterRegistry(configs, registryEventConsumer);
    }

    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
                                  RegistryEventConsumer<TimeLimiter> registryEventConsumer,
                                  io.vavr.collection.Map<String, String> tags) {
        return new InMemoryTimeLimiterRegistry(configs, registryEventConsumer, tags);
    }

    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
                                  List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers) {
        return new InMemoryTimeLimiterRegistry(configs, registryEventConsumers);
    }

    Seq<TimeLimiter> getAllTimeLimiters();

    TimeLimiter timeLimiter(String name);

    TimeLimiter timeLimiter(String name, io.vavr.collection.Map<String, String> tags);

    TimeLimiter timeLimiter(String name, TimeLimiterConfig config);

    TimeLimiter timeLimiter(String name, TimeLimiterConfig config,
                            io.vavr.collection.Map<String, String> tags);

    TimeLimiter timeLimiter(String name, Supplier<TimeLimiterConfig> configSupplier);

    TimeLimiter timeLimiter(String name, Supplier<TimeLimiterConfig> configSupplier,
                            io.vavr.collection.Map<String, String> tags);

    TimeLimiter timeLimiter(String name, String configName);

    TimeLimiter timeLimiter(String name, String configName,
                            io.vavr.collection.Map<String, String> tags);
}
