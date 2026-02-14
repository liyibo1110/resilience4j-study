package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 10:27
 */
public class TaggedRetryMetrics extends AbstractRetryMetrics implements MeterBinder {
    private final RetryRegistry retryRegistry;

    private TaggedRetryMetrics(RetryMetricNames names, RetryRegistry retryRegistry) {
        super(names);
        this.retryRegistry = requireNonNull(retryRegistry);
    }

    public static TaggedRetryMetrics ofRetryRegistry(RetryRegistry retryRegistry) {
        return new TaggedRetryMetrics(RetryMetricNames.ofDefaults(), retryRegistry);
    }

    public static TaggedRetryMetrics ofRetryRegistry(RetryMetricNames names, RetryRegistry retryRegistry) {
        return new TaggedRetryMetrics(names, retryRegistry);
    }

    @Deprecated
    public static TaggedRetryMetrics ofRetryRegistry(MetricNames names, RetryRegistry retryRegistry) {
        return new TaggedRetryMetrics(names, retryRegistry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for(Retry retry : retryRegistry.getAllRetries())
            addMetrics(registry, retry);

        retryRegistry.getEventPublisher().onEntryAdded(event -> addMetrics(registry, event.getAddedEntry()));
        retryRegistry.getEventPublisher().onEntryRemoved(event -> removeMetrics(registry, event.getRemovedEntry().getName()));
        retryRegistry.getEventPublisher().onEntryReplaced(event -> {
            removeMetrics(registry, event.getOldEntry().getName());
            addMetrics(registry, event.getNewEntry());
        });
    }
}
