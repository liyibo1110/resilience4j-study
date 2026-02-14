package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 10:29
 */
public class TaggedTimeLimiterMetrics extends AbstractTimeLimiterMetrics implements MeterBinder {
    private final TimeLimiterRegistry timeLimiterRegistry;

    private TaggedTimeLimiterMetrics(TimeLimiterMetricNames names, TimeLimiterRegistry timeLimiterRegistry) {
        super(names);
        this.timeLimiterRegistry = requireNonNull(timeLimiterRegistry);
    }

    public static TaggedTimeLimiterMetrics ofTimeLimiterRegistry(TimeLimiterRegistry timeLimiterRegistry) {
        return new TaggedTimeLimiterMetrics(TimeLimiterMetricNames.ofDefaults(), timeLimiterRegistry);
    }

    public static TaggedTimeLimiterMetrics ofTimeLimiterRegistry(TimeLimiterMetricNames names, TimeLimiterRegistry timeLimiterRegistry) {
        return new TaggedTimeLimiterMetrics(names, timeLimiterRegistry);
    }

    @Deprecated
    public static TaggedTimeLimiterMetrics ofTimeLimiterRegistry(MetricNames names, TimeLimiterRegistry timeLimiterRegistry) {
        return new TaggedTimeLimiterMetrics(names, timeLimiterRegistry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for(TimeLimiter timeLimiter : timeLimiterRegistry.getAllTimeLimiters())
            addMetrics(registry, timeLimiter);

        timeLimiterRegistry.getEventPublisher().onEntryAdded(event -> addMetrics(registry, event.getAddedEntry()));
        timeLimiterRegistry.getEventPublisher().onEntryRemoved(event -> removeMetrics(registry, event.getRemovedEntry().getName()));
        timeLimiterRegistry.getEventPublisher().onEntryReplaced(event -> {
            removeMetrics(registry, event.getOldEntry().getName());
            addMetrics(registry, event.getNewEntry());
        });
    }
}
