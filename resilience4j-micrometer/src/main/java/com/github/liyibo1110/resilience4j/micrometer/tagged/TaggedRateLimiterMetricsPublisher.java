package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.core.metrics.MetricsPublisher;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import io.micrometer.core.instrument.MeterRegistry;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 11:17
 */
public class TaggedRateLimiterMetricsPublisher extends AbstractRateLimiterMetrics implements MetricsPublisher<RateLimiter> {
    private final MeterRegistry meterRegistry;

    public TaggedRateLimiterMetricsPublisher(MeterRegistry meterRegistry) {
        super(RateLimiterMetricNames.ofDefaults());
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    public TaggedRateLimiterMetricsPublisher(RateLimiterMetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Deprecated
    public TaggedRateLimiterMetricsPublisher(MetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Override
    public void publishMetrics(RateLimiter entry) {
        addMetrics(meterRegistry, entry);
    }

    @Override
    public void removeMetrics(RateLimiter entry) {
        removeMetrics(meterRegistry, entry.getName());
    }
}
