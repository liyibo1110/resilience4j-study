package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 10:28
 */
public class TaggedThreadPoolBulkheadMetrics extends AbstractThreadPoolBulkheadMetrics implements MeterBinder {
    private final ThreadPoolBulkheadRegistry bulkheadRegistry;

    private TaggedThreadPoolBulkheadMetrics(ThreadPoolBulkheadMetricNames names,
                                            ThreadPoolBulkheadRegistry bulkheadRegistry) {
        super(names);
        this.bulkheadRegistry = requireNonNull(bulkheadRegistry);
    }

    public static TaggedThreadPoolBulkheadMetrics ofThreadPoolBulkheadRegistry(ThreadPoolBulkheadRegistry bulkheadRegistry) {
        return new TaggedThreadPoolBulkheadMetrics(ThreadPoolBulkheadMetricNames.ofDefaults(), bulkheadRegistry);
    }

    public static TaggedThreadPoolBulkheadMetrics ofThreadPoolBulkheadRegistry(ThreadPoolBulkheadMetricNames names,
                                                                               ThreadPoolBulkheadRegistry bulkheadRegistry) {
        return new TaggedThreadPoolBulkheadMetrics(names, bulkheadRegistry);
    }

    @Deprecated
    public static TaggedThreadPoolBulkheadMetrics ofThreadPoolBulkheadRegistry(MetricNames names, ThreadPoolBulkheadRegistry bulkheadRegistry) {
        return new TaggedThreadPoolBulkheadMetrics(names, bulkheadRegistry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for(ThreadPoolBulkhead bulkhead : bulkheadRegistry.getAllBulkheads())
            addMetrics(registry, bulkhead);

        bulkheadRegistry.getEventPublisher().onEntryAdded(event -> addMetrics(registry, event.getAddedEntry()));
        bulkheadRegistry.getEventPublisher().onEntryRemoved(event -> removeMetrics(registry, event.getRemovedEntry().getName()));
        bulkheadRegistry.getEventPublisher().onEntryReplaced(event -> {
            removeMetrics(registry, event.getOldEntry().getName());
            addMetrics(registry, event.getNewEntry());
        });
    }
}
