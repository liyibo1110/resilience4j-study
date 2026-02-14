package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
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
 * @date 2026-02-14 10:05
 */
abstract class AbstractRateLimiterMetrics extends AbstractMetrics {
    protected final RateLimiterMetricNames names;

    protected AbstractRateLimiterMetrics(RateLimiterMetricNames names) {
        this.names = requireNonNull(names);
    }

    @Deprecated
    protected AbstractRateLimiterMetrics(MetricNames names) {
        this.names = requireNonNull(names);
    }

    protected void addMetrics(MeterRegistry meterRegistry, RateLimiter rateLimiter) {
        List<Tag> customTags = mapToTagsList(rateLimiter.getTags().toJavaMap());
        registerMetrics(meterRegistry, rateLimiter, customTags);
    }

    private void registerMetrics(MeterRegistry meterRegistry, RateLimiter rateLimiter, List<Tag> customTags) {
        removeMetrics(meterRegistry, rateLimiter.getName());

        Set<Meter.Id> idSet = new HashSet<>();
        idSet.add(Gauge.builder(names.getAvailablePermissionsMetricName(), rateLimiter, rl -> rl.getMetrics().getAvailablePermissions())
                .description("The number of available permissions")
                .tag(TagNames.NAME, rateLimiter.getName())
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(Gauge.builder(names.getWaitingThreadsMetricName(), rateLimiter, rl -> rl.getMetrics().getNumberOfWaitingThreads())
                .description("The number of waiting threads")
                .tag(TagNames.NAME, rateLimiter.getName())
                .tags(customTags)
                .register(meterRegistry).getId());

        meterIdMap.put(rateLimiter.getName(), idSet);
    }

    @Deprecated
    public static class MetricNames extends RateLimiterMetricNames { }
}
