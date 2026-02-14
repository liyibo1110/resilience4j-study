package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.retry.Retry;
import io.micrometer.core.instrument.FunctionCounter;
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
abstract class AbstractRetryMetrics extends AbstractMetrics {
    protected final RetryMetricNames names;

    protected AbstractRetryMetrics(RetryMetricNames names) {
        this.names = requireNonNull(names);
    }

    @Deprecated
    protected AbstractRetryMetrics(MetricNames names) {
        this.names = requireNonNull(names);
    }

    protected void addMetrics(MeterRegistry meterRegistry, Retry retry) {
        List<Tag> customTags = mapToTagsList(retry.getTags().toJavaMap());
        registerMetrics(meterRegistry, retry, customTags);
    }

    private void registerMetrics(MeterRegistry meterRegistry, Retry retry, List<Tag> customTags) {
        removeMetrics(meterRegistry, retry.getName());

        Set<Meter.Id> idSet = new HashSet<>();
        idSet.add(FunctionCounter.builder(names.getCallsMetricName(), retry, rt -> rt.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt())
                .description("The number of successful calls without a retry attempt")
                .tag(TagNames.NAME, retry.getName())
                .tag(TagNames.KIND, "successful_without_retry")
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(FunctionCounter.builder(names.getCallsMetricName(), retry, rt -> rt.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt())
                .description("The number of successful calls after a retry attempt")
                .tag(TagNames.NAME, retry.getName())
                .tag(TagNames.KIND, "successful_with_retry")
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(FunctionCounter.builder(names.getCallsMetricName(), retry, rt -> rt.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt())
                .description("The number of failed calls without a retry attempt")
                .tag(TagNames.NAME, retry.getName())
                .tag(TagNames.KIND, "failed_without_retry")
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(FunctionCounter.builder(names.getCallsMetricName(), retry, rt -> rt.getMetrics().getNumberOfFailedCallsWithRetryAttempt())
                .description("The number of failed calls after a retry attempt")
                .tag(TagNames.NAME, retry.getName())
                .tag(TagNames.KIND, "failed_with_retry")
                .tags(customTags)
                .register(meterRegistry).getId());
        meterIdMap.put(retry.getName(), idSet);
    }

    @Deprecated
    public static class MetricNames extends RetryMetricNames {}
}
