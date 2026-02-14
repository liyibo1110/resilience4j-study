package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.core.metrics.MetricsPublisher;
import com.github.liyibo1110.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 11:17
 */
public class TaggedRetryMetricsPublisher extends AbstractRetryMetrics implements MetricsPublisher<Retry> {
    private final MeterRegistry meterRegistry;

    public TaggedRetryMetricsPublisher(MeterRegistry meterRegistry) {
        super(RetryMetricNames.ofDefaults());
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    public TaggedRetryMetricsPublisher(RetryMetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Deprecated
    public TaggedRetryMetricsPublisher(MetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Override
    public void publishMetrics(Retry entry) {
        addMetrics(meterRegistry, entry);
    }

    @Override
    public void removeMetrics(Retry entry) {
        removeMetrics(meterRegistry, entry.getName());
    }
}
