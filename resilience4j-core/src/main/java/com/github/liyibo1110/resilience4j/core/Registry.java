package com.github.liyibo1110.resilience4j.core;

import com.github.liyibo1110.resilience4j.core.registry.EntryAddedEvent;
import com.github.liyibo1110.resilience4j.core.registry.EntryRemovedEvent;
import com.github.liyibo1110.resilience4j.core.registry.EntryReplacedEvent;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEvent;
import io.vavr.collection.Map;

import java.util.Optional;

/**
 * 核心注册表，以实现通用功能
 * @author liyibo
 * @date 2026-02-04 14:18
 */
public interface Registry<E, C> {

    /**
     * 新增一个配置（Configuration）
     */
    void addConfiguration(String configName, C configuration);

    /**
     * 在registry中尝试查找name对应的entry
     */
    Optional<E> find(String name);

    Optional<E> remove(String name);

    Optional<E> replace(String name, E newEntry);

    Optional<C> getConfiguration(String configName);

    /**
     * 获取默认的Configuration
     */
    C getDefaultConfig();

    /**
     * 获取全局registry tags
     */
    Map<String, String> getTags();

    /**
     * 返回一个可用于注册事件消费者的EventPublisher实现（注意是在内部定义的这个接口）
     */
    EventPublisher<E> getEventPublisher();

    /**
     * 专门用于register事件消费的publisher
     * @param <E>
     */
    interface EventPublisher<E> extends com.github.liyibo1110.resilience4j.core.EventPublisher<RegistryEvent> {
        EventPublisher<E> onEntryAdded(EventConsumer<EntryAddedEvent<E>> eventConsumer);
        EventPublisher<E> onEntryRemoved(EventConsumer<EntryRemovedEvent<E>> eventConsumer);
        EventPublisher<E> onEntryReplaced(EventConsumer<EntryReplacedEvent<E>> eventConsumer);
    }
}
