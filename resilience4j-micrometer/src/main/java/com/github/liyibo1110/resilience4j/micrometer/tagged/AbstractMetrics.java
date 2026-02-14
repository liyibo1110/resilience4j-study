package com.github.liyibo1110.resilience4j.micrometer.tagged;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-13 15:38
 */
abstract class AbstractMetrics {
    protected ConcurrentMap<String, Set<Meter.Id>> meterIdMap;

    AbstractMetrics() {
        this.meterIdMap = new ConcurrentHashMap<>();
    }

    /**
     * 移除指定name的所有指标
     */
    void removeMetrics(MeterRegistry registry, String name) {
        Set<Meter.Id> ids = this.meterIdMap.get(name);
        if(ids != null)
            ids.forEach(registry::remove);
        meterIdMap.remove(name);
    }

    /**
     * tagMap -> tagList
     */
    List<Tag> mapToTagsList(Map<String, String> tagsMap) {
        return tagsMap.entrySet()
                .stream().map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
