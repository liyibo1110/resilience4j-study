package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 10:25
 */
public class TaggedBulkheadMetrics extends AbstractBulkheadMetrics implements MeterBinder {
    private final BulkheadRegistry bulkheadRegistry;

    private TaggedBulkheadMetrics(BulkheadMetricNames names, BulkheadRegistry bulkheadRegistry) {
        super(names);
        this.bulkheadRegistry = requireNonNull(bulkheadRegistry);
    }

    public static TaggedBulkheadMetrics ofBulkheadRegistry(BulkheadRegistry bulkheadRegistry) {
        return new TaggedBulkheadMetrics(BulkheadMetricNames.ofDefaults(), bulkheadRegistry);
    }

    public static TaggedBulkheadMetrics ofBulkheadRegistry(BulkheadMetricNames names, BulkheadRegistry bulkheadRegistry) {
        return new TaggedBulkheadMetrics(names, bulkheadRegistry);
    }

    @Deprecated
    public static TaggedBulkheadMetrics ofBulkheadRegistry(MetricNames names, BulkheadRegistry bulkheadRegistry) {
        return new TaggedBulkheadMetrics(names, bulkheadRegistry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for(Bulkhead bulkhead : bulkheadRegistry.getAllBulkheads())
            addMetrics(registry, bulkhead);

        bulkheadRegistry.getEventPublisher().onEntryAdded(event -> addMetrics(registry, event.getAddedEntry()));
        bulkheadRegistry.getEventPublisher().onEntryRemoved(event -> removeMetrics(registry, event.getRemovedEntry().getName()));
        bulkheadRegistry.getEventPublisher().onEntryReplaced(event -> {
            removeMetrics(registry, event.getOldEntry().getName());
            addMetrics(registry, event.getNewEntry());
        });
    }
}
