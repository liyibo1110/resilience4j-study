package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.core.metrics.MetricsPublisher;
import io.micrometer.core.instrument.MeterRegistry;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 11:16
 */
public class TaggedBulkheadMetricsPublisher extends AbstractBulkheadMetrics implements MetricsPublisher<Bulkhead> {
    private final MeterRegistry meterRegistry;

    public TaggedBulkheadMetricsPublisher(MeterRegistry meterRegistry) {
        super(BulkheadMetricNames.ofDefaults());
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    public TaggedBulkheadMetricsPublisher(BulkheadMetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Deprecated
    public TaggedBulkheadMetricsPublisher(MetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Override
    public void publishMetrics(Bulkhead entry) {
        addMetrics(meterRegistry, entry);
    }

    @Override
    public void removeMetrics(Bulkhead entry) {
        removeMetrics(meterRegistry, entry.getName());
    }
}
