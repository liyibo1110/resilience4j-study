package com.github.liyibo1110.resilience4j.core.registry;

import com.github.liyibo1110.resilience4j.core.RegistryStore;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 基于ConcurrentHashMap的RegistryStore实现
 * @author liyibo
 * @date 2026-02-04 14:52
 */
public class InMemoryRegistryStore<E> implements RegistryStore<E> {
    private final ConcurrentMap<String, E> entryMap;

    public InMemoryRegistryStore() {
        this.entryMap = new ConcurrentHashMap<>();
    }


    @Override
    public E computeIfAbsent(String key, Function<? super String, ? extends E> mappingFunction) {
        return this.entryMap.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public E putIfAbsent(String key, E value) {
        return this.entryMap.putIfAbsent(key, value);
    }

    @Override
    public Optional<E> find(String key) {
        return Optional.ofNullable(this.entryMap.get(key));
    }

    @Override
    public Optional<E> remove(String name) {
        return Optional.ofNullable(this.entryMap.remove(name));
    }

    @Override
    public Optional<E> replace(String name, E newEntry) {
        return Optional.ofNullable(this.entryMap.replace(name, newEntry));
    }

    @Override
    public Collection<E> values() {
        return this.entryMap.values();
    }
}
