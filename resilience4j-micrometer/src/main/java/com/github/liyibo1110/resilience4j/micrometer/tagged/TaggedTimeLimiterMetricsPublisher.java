package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.core.metrics.MetricsPublisher;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import io.micrometer.core.instrument.MeterRegistry;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 11:19
 */
public class TaggedTimeLimiterMetricsPublisher extends AbstractTimeLimiterMetrics implements MetricsPublisher<TimeLimiter> {
    private final MeterRegistry meterRegistry;

    public TaggedTimeLimiterMetricsPublisher(MeterRegistry meterRegistry) {
        super(TimeLimiterMetricNames.ofDefaults());
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    public TaggedTimeLimiterMetricsPublisher(TimeLimiterMetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Deprecated
    public TaggedTimeLimiterMetricsPublisher(MetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Override
    public void publishMetrics(TimeLimiter entry) {
        addMetrics(meterRegistry, entry);
    }

    @Override
    public void removeMetrics(TimeLimiter entry) {
        removeMetrics(meterRegistry, entry.getName());
    }
}
