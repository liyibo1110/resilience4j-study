package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-14 10:07
 */
abstract class AbstractTimeLimiterMetrics extends AbstractMetrics {
    private static final String KIND_FAILED = "failed";
    private static final String KIND_SUCCESSFUL = "successful";
    private static final String KIND_TIMEOUT = "timeout";

    protected final TimeLimiterMetricNames names;

    protected AbstractTimeLimiterMetrics(TimeLimiterMetricNames names) {
        this.names = requireNonNull(names);
    }

    @Deprecated
    protected AbstractTimeLimiterMetrics(MetricNames names) {
        this.names = requireNonNull(names);
    }

    protected void addMetrics(MeterRegistry meterRegistry, TimeLimiter timeLimiter) {
        List<Tag> customTags = mapToTagsList(timeLimiter.getTags().toJavaMap());
        registerMetrics(meterRegistry, timeLimiter, customTags);
    }

    protected void registerMetrics(MeterRegistry meterRegistry, TimeLimiter timeLimiter, List<Tag> customTags) {
        removeMetrics(meterRegistry, timeLimiter.getName());

        Counter successes = Counter.builder(names.getCallsMetricName())
                .description("The number of successful calls")
                .tag(TagNames.NAME, timeLimiter.getName())
                .tag(TagNames.KIND, KIND_SUCCESSFUL)
                .tags(customTags)
                .register(meterRegistry);
        Counter failures = Counter.builder(names.getCallsMetricName())
                .description("The number of failed calls")
                .tag(TagNames.NAME, timeLimiter.getName())
                .tag(TagNames.KIND, KIND_FAILED)
                .tags(customTags)
                .register(meterRegistry);
        Counter timeouts = Counter.builder(names.getCallsMetricName())
                .description("The number of timed out calls")
                .tag(TagNames.NAME, timeLimiter.getName())
                .tag(TagNames.KIND, KIND_TIMEOUT)
                .tags(customTags)
                .register(meterRegistry);

        timeLimiter.getEventPublisher()
                .onSuccess(event -> successes.increment())
                .onError(event -> failures.increment())
                .onTimeout(event -> timeouts.increment());

        List<Meter.Id> ids = Arrays.asList(successes.getId(), failures.getId(), timeouts.getId());
        meterIdMap.put(timeLimiter.getName(), new HashSet<>(ids));
    }

    @Deprecated
    public static class MetricNames extends TimeLimiterMetricNames {}
}
