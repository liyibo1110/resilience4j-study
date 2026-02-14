package com.github.liyibo1110.resilience4j.core.registry;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.EventProcessor;
import com.github.liyibo1110.resilience4j.core.Registry;
import com.github.liyibo1110.resilience4j.core.RegistryStore;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Registry接口的骨架实现
 * @author liyibo
 * @date 2026-02-04 15:07
 */
public class AbstractRegistry<E, C> implements Registry<E, C> {
    protected static final String DEFAULT_CONFIG = "default";
    protected static final String CONFIG_MUST_NOT_BE_NULL = "Config must not be null";
    protected static final String CONSUMER_MUST_NOT_BE_NULL = "EventConsumers must not be null";
    protected static final String SUPPLIER_MUST_NOT_BE_NULL = "Supplier must not be null";
    protected static final String TAGS_MUST_NOT_BE_NULL = "Tags must not be null";
    private static final String NAME_MUST_NOT_BE_NULL = "Name must not be null";
    private static final String REGISTRY_STORE_MUST_NOT_BE_NULL = "Registry Store must not be null";

    /** ，每个实例对*/

    /**
     * 用来存储被管理的resilience组件实例（就是entry这个东西，内含自己的metrics/state/eventPublisher）
     * 例如CircuitBreaker，每个实例会对应下面字段的一个模板，没有指定就是用default模板
     */
    protected final RegistryStore<E> entryMap;

    /** 用来存储各种功能的配置模板，value是XXXConfig对象的实例，例如CircuitBreakerConfig，可以类比BeanDefinition */
    protected final ConcurrentMap<String, C> configurations;

    /** 注意这个Map不是JDK自带的 */
    protected final Map<String, String> registryTags;

    private final RegistryEventProcessor eventProcessor;

    public AbstractRegistry(C defaultConfig) {
        this(defaultConfig, HashMap.empty());
    }

    public AbstractRegistry(C defaultConfig, Map<String, String> registryTags) {
        this(defaultConfig, new ArrayList<>(), registryTags);
    }

    public AbstractRegistry(C defaultConfig, RegistryEventConsumer<E> consumers) {
        this(defaultConfig, consumers, HashMap.empty());
    }

    public AbstractRegistry(C defaultConfig, RegistryEventConsumer<E> consumers, Map<String, String> tags) {
        this(defaultConfig, Collections.singletonList(Objects.requireNonNull(consumers, CONSUMER_MUST_NOT_BE_NULL)), tags);
    }

    public AbstractRegistry(C defaultConfig, List<RegistryEventConsumer<E>> consumers) {
        this(defaultConfig, consumers, HashMap.empty());
    }

    public AbstractRegistry(C defaultConfig, List<RegistryEventConsumer<E>> consumers, Map<String, String> tags) {
        this.configurations = new ConcurrentHashMap<>();
        this.entryMap = new InMemoryRegistryStore<>();

        Objects.requireNonNull(consumers, CONSUMER_MUST_NOT_BE_NULL);
        this.eventProcessor = new RegistryEventProcessor(consumers);

        Objects.requireNonNull(tags, TAGS_MUST_NOT_BE_NULL);
        this.registryTags = tags;

        Objects.requireNonNull(defaultConfig, CONFIG_MUST_NOT_BE_NULL);
        this.configurations.put(DEFAULT_CONFIG, defaultConfig);
    }

    public AbstractRegistry(C defaultConfig, List<RegistryEventConsumer<E>> consumers,
                            Map<String, String> tags, RegistryStore<E> registryStore) {
        this.configurations = new ConcurrentHashMap<>();

        Objects.requireNonNull(registryStore, REGISTRY_STORE_MUST_NOT_BE_NULL);
        this.entryMap = registryStore;

        Objects.requireNonNull(consumers, CONSUMER_MUST_NOT_BE_NULL);
        this.eventProcessor = new RegistryEventProcessor(consumers);

        Objects.requireNonNull(tags, TAGS_MUST_NOT_BE_NULL);
        this.registryTags = tags;

        Objects.requireNonNull(defaultConfig, CONFIG_MUST_NOT_BE_NULL);
        this.configurations.put(DEFAULT_CONFIG, defaultConfig);
    }

    /**
     * 向entryMap尝试写入新entry，其中value是由supplier生成的（注意和name是没有关系的，因为靠name算不出entry）
     */
    protected E computeIfAbsent(String name, Supplier<E> supplier) {
        Objects.requireNonNull(name, NAME_MUST_NOT_BE_NULL);
        return this.entryMap.computeIfAbsent(name, k -> {
            E entry = supplier.get();
            // 这里要通知add监听器（非常重要的调用点，涉及到micrometer的关键对接）
            this.eventProcessor.processEvent(new EntryAddedEvent<>(entry));
            return entry;
        });
    }

    @Override
    public Optional<E> find(String name) {
        return this.entryMap.find(name);
    }

    @Override
    public Optional<E> remove(String name) {
        Optional<E> removedEntry = entryMap.remove(name);
        // 这里要通知remove监听器（前提是真的remove了）
        removedEntry.ifPresent(entry -> this.eventProcessor.processEvent(new EntryRemovedEvent<>(entry)));
        return removedEntry;
    }

    @Override
    public Optional<E> replace(String name, E newEntry) {
        Optional<E> replacedEntry = entryMap.replace(name, newEntry);
        // 这里要通知replace监听器（前提是真的replace了）
        replacedEntry.ifPresent(oldEntry -> this.eventProcessor.processEvent(new EntryReplacedEvent<>(oldEntry, newEntry)));
        return replacedEntry;
    }

    @Override
    public void addConfiguration(String configName, C configuration) {
        if(configName.equals(DEFAULT_CONFIG))
            throw new IllegalArgumentException("You cannot use 'default' as a configuration name as it is preserved for default configuration");
        this.configurations.put(configName, configuration);
    }

    @Override
    public Optional<C> getConfiguration(String configName) {
        return Optional.ofNullable(this.configurations.get(configName));
    }

    @Override
    public C getDefaultConfig() {
        return this.configurations.get(DEFAULT_CONFIG);
    }

    @Override
    public Map<String, String> getTags() {
        return this.registryTags;
    }

    @Override
    public EventPublisher<E> getEventPublisher() {
        return this.eventProcessor;
    }

    /**
     * 将给定的map，添加到实例本身的registryTags后，再返回整个结果
     * @param tags
     * @return
     */
    protected Map<String, String> getAllTags(Map<String, String> tags) {
        Objects.requireNonNull(tags, TAGS_MUST_NOT_BE_NULL);
        return tags.merge(this.registryTags);
    }

    /**
     * 既是processor，同时也是一个EventConsumer，而且还是Registry.EventPublisher（三合一）
     * 只能在AbstractRegistry内部被构造和使用（所以不是static的）
     */
    private class RegistryEventProcessor extends EventProcessor<RegistryEvent>
            implements EventConsumer<RegistryEvent>, Registry.EventPublisher<E> {

        private RegistryEventProcessor() {}

        private RegistryEventProcessor(List<RegistryEventConsumer<E>> consumers) {
            // 分3种类型绑定各自的消费者方法
            consumers.forEach(c -> {
                this.onEntryAdded(c::onEntryAddedEvent);
                this.onEntryRemoved(c::onEntryRemovedEvent);
                this.onEntryReplaced(c::onEntryReplacedEvent);
            });
        }

        @Override
        public EventPublisher<E> onEntryAdded(EventConsumer<EntryAddedEvent<E>> onSuccessEventConsumer) {
            this.registerConsumer(EntryAddedEvent.class.getName(), onSuccessEventConsumer);
            return this;
        }

        @Override
        public EventPublisher<E> onEntryRemoved(EventConsumer<EntryRemovedEvent<E>> onErrorEventConsumer) {
            this.registerConsumer(EntryRemovedEvent.class.getName(), onErrorEventConsumer);
            return this;
        }

        @Override
        public EventPublisher<E> onEntryReplaced(EventConsumer<EntryReplacedEvent<E>> onStateTransitionEventConsumer) {
            this.registerConsumer(EntryReplacedEvent.class.getName(), onStateTransitionEventConsumer);
            return this;
        }

        @Override
        public void consumeEvent(RegistryEvent event) {
            // 直接调用父级的方法
            super.processEvent(event);
        }
    }
}
