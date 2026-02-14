package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 10:26
 */
public class TaggedRateLimiterMetrics extends AbstractRateLimiterMetrics implements MeterBinder {
    private final RateLimiterRegistry rateLimiterRegistry;

    private TaggedRateLimiterMetrics(RateLimiterMetricNames names, RateLimiterRegistry rateLimiterRegistry) {
        super(names);
        this.rateLimiterRegistry = requireNonNull(rateLimiterRegistry);
    }

    public static TaggedRateLimiterMetrics ofRateLimiterRegistry(RateLimiterRegistry rateLimiterRegistry) {
        return new TaggedRateLimiterMetrics(RateLimiterMetricNames.ofDefaults(), rateLimiterRegistry);
    }

    public static TaggedRateLimiterMetrics ofRateLimiterRegistry(RateLimiterMetricNames names, RateLimiterRegistry rateLimiterRegistry) {
        return new TaggedRateLimiterMetrics(names, rateLimiterRegistry);
    }

    @Deprecated
    public static TaggedRateLimiterMetrics ofRateLimiterRegistry(MetricNames names, RateLimiterRegistry rateLimiterRegistry) {
        return new TaggedRateLimiterMetrics(names, rateLimiterRegistry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for(RateLimiter rateLimiter : rateLimiterRegistry.getAllRateLimiters())
            addMetrics(registry, rateLimiter);

        rateLimiterRegistry.getEventPublisher().onEntryAdded(event -> addMetrics(registry, event.getAddedEntry()));
        rateLimiterRegistry.getEventPublisher().onEntryRemoved(event -> removeMetrics(registry, event.getRemovedEntry().getName()));
        rateLimiterRegistry.getEventPublisher().onEntryReplaced(event -> {
            removeMetrics(registry, event.getOldEntry().getName());
            addMetrics(registry, event.getNewEntry());
        });
    }
}
