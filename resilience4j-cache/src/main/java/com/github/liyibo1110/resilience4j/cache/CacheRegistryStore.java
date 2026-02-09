package com.github.liyibo1110.resilience4j.cache;

import com.github.liyibo1110.resilience4j.core.RegistryStore;

import javax.cache.Cache;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * 基于JCache接口的RegistryStore实现
 * @author liyibo
 * @date 2026-02-09 14:40
 */
public class CacheRegistryStore<E> implements RegistryStore<E> {

    private final Cache<String, E> cacheStore;

    public CacheRegistryStore(Cache<String, E> cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public E computeIfAbsent(String key, Function<? super String, ? extends E> mappingFunction) {
        try {
            return this.cacheStore.invoke(key, new AtomicComputeProcessor<>(), mappingFunction);
        } catch (EntryProcessorException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public E putIfAbsent(String key, E value) {
        return this.computeIfAbsent(key, k -> value);
    }

    @Override
    public Optional<E> find(String key) {
        return Optional.ofNullable(this.cacheStore.get(key));
    }

    @Override
    public Optional<E> remove(String name) {
        return Optional.ofNullable(this.cacheStore.getAndRemove(name));
    }

    @Override
    public Optional<E> replace(String name, E newEntry) {
        return Optional.ofNullable(this.cacheStore.getAndReplace(name, newEntry));
    }

    @Override
    public Collection<E> values() {
        Collection<E> values = new ArrayList<>();
        this.cacheStore.iterator().forEachRemaining(iter -> values.add(iter.getValue()));
        return values;
    }

    /**
     * JCache的核心类，特点是内部自带原子性，不需要显式加锁
     */
    static class AtomicComputeProcessor<String, E> implements EntryProcessor<String, E, E> {
        @Override
        public E process(MutableEntry<String, E> entry, Object... arguments) throws EntryProcessorException {
            // 就是这样规定的顺序，用来生成新的value
            Function<? super String, ? extends E> mappingFunction = (Function<? super String, ? extends E>)arguments[0];
            E oldValue = entry.getValue();
            if(oldValue != null)    // key存在不为null的value，则直接返回，不写入
                return oldValue;

            E newValue = mappingFunction.apply(entry.getKey());
            if(newValue != null) {
                entry.setValue(newValue);
                return newValue;
            }else {
                return oldValue;
            }
        }
    }
}
