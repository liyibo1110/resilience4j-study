package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 10:04
 */
abstract class AbstractBulkheadMetrics extends AbstractMetrics {
    protected final BulkheadMetricNames names;

    protected AbstractBulkheadMetrics(BulkheadMetricNames names) {
        this.names = requireNonNull(names);
    }

    @Deprecated
    protected AbstractBulkheadMetrics(MetricNames names) {
        this.names = requireNonNull(names);
    }

    protected void addMetrics(MeterRegistry meterRegistry, Bulkhead bulkhead) {
        List<Tag> customTags = mapToTagsList(bulkhead.getTags().toJavaMap());
        registerMetrics(meterRegistry, bulkhead, customTags);
    }

    private void registerMetrics(MeterRegistry meterRegistry, Bulkhead bulkhead, List<Tag> customTags) {
        removeMetrics(meterRegistry, bulkhead.getName());

        Set<Meter.Id> idSet = new HashSet<>();
        idSet.add(Gauge.builder(names.getAvailableConcurrentCallsMetricName(), bulkhead, bh -> bh.getMetrics().getAvailableConcurrentCalls())
                .description("The number of available permissions")
                .tag(TagNames.NAME, bulkhead.getName())
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(Gauge.builder(names.getMaxAllowedConcurrentCallsMetricName(), bulkhead, bh -> bh.getMetrics().getMaxAllowedConcurrentCalls())
                .description("The maximum number of available permissions")
                .tag(TagNames.NAME, bulkhead.getName())
                .tags(customTags)
                .register(meterRegistry).getId());

        meterIdMap.put(bulkhead.getName(), idSet);
    }

    @Deprecated
    public static class MetricNames extends BulkheadMetricNames {}
}
