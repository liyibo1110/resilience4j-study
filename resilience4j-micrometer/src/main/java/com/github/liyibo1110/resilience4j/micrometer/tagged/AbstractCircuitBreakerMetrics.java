package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-13 15:43
 */
abstract class AbstractCircuitBreakerMetrics extends AbstractMetrics {
    private static final String KIND_STATE = "state";
    private static final String KIND_FAILED = "failed";
    private static final String KIND_SUCCESSFUL = "successful";
    private static final String KIND_IGNORED = "ignored";
    private static final String KIND_NOT_PERMITTED = "not_permitted";
    protected final CircuitBreakerMetricNames names;

    protected AbstractCircuitBreakerMetrics(CircuitBreakerMetricNames names) {
        this.names = requireNonNull(names);
    }

    @Deprecated
    protected AbstractCircuitBreakerMetrics(MetricNames names) {
        this.names = requireNonNull(names);
    }

    /**
     * 将指定cb对象的metrics指标，注册到特定的MeterRegistry
     */
    protected void addMetrics(MeterRegistry meterRegistry, CircuitBreaker cb) {
        List<Tag> customTags = mapToTagsList(cb.getTags().toJavaMap());
        registerMetrics(meterRegistry, cb, customTags);
    }

    /**
     * 核心方法：将指定cb对象的metrics指标及其tags，注册到特定的MeterRegistry
     */
    private void registerMetrics(MeterRegistry meterRegistry, CircuitBreaker circuitBreaker, List<Tag> customTags) {
        // 先尝试remove之前注册过的
        removeMetrics(meterRegistry, circuitBreaker.getName());

        Set<Meter.Id> idSet = new HashSet<>();
        // cb的每种状态都要处理
        final CircuitBreaker.State[] states = CircuitBreaker.State.values();
        for(CircuitBreaker.State state : states) {
            idSet.add(Gauge.builder(names.getStateMetricName(), circuitBreaker, cb -> cb.getState() == state ? 1 : 0)
                    .description("The states of the circuit breaker")
                    .tag(TagNames.NAME, circuitBreaker.getName())
                    .tag(KIND_STATE, state.name().toLowerCase())
                    .tags(customTags)
                    .register(meterRegistry).getId());
        }
        idSet.add(Gauge.builder(names.getBufferedCallsMetricName(), circuitBreaker, cb -> cb.getMetrics().getNumberOfFailedCalls())
                .description("The number of buffered failed calls stored in the ring buffer")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_FAILED)
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(Gauge.builder(names.getBufferedCallsMetricName(), circuitBreaker, cb -> cb.getMetrics().getNumberOfSuccessfulCalls())
                .description("The number of buffered successful calls stored in the ring buffer")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_SUCCESSFUL)
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(Gauge.builder(names.getSlowCallsMetricName(), circuitBreaker, cb -> cb.getMetrics().getNumberOfSlowSuccessfulCalls())
                .description("The number of slow successful which were slower than a certain threshold")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_SUCCESSFUL)
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(Gauge.builder(names.getSlowCallsMetricName(), circuitBreaker, cb -> cb.getMetrics().getNumberOfSlowFailedCalls())
                .description("The number of slow failed calls which were slower than a certain threshold")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_FAILED)
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(Gauge.builder(names.getFailureRateMetricName(), circuitBreaker, cb -> cb.getMetrics().getFailureRate())
                .description("The failure rate of the circuit breaker")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tags(customTags)
                .register(meterRegistry).getId());
        idSet.add(Gauge.builder(names.getSlowCallRateMetricName(), circuitBreaker, cb -> cb.getMetrics().getSlowCallRate())
                .description("The slow call of the circuit breaker")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tags(customTags)
                .register(meterRegistry).getId());

        Timer successfulCalls = Timer.builder(names.getCallsMetricName())
                .description("Total number of successful calls")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_SUCCESSFUL)
                .tags(customTags)
                .register(meterRegistry);

        Timer failedCalls = Timer.builder(names.getCallsMetricName())
                .description("Total number of failed calls")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_FAILED)
                .tags(customTags)
                .register(meterRegistry);

        Timer ignoredFailedCalls = Timer.builder(names.getCallsMetricName())
                .description("Total number of calls which failed but the exception was ignored")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_IGNORED)
                .tags(customTags)
                .register(meterRegistry);

        Counter notPermittedCalls = Counter.builder(names.getNotPermittedCallsMetricName())
                .description("Total number of not permitted calls")
                .tag(TagNames.NAME, circuitBreaker.getName())
                .tag(TagNames.KIND, KIND_NOT_PERMITTED)
                .tags(customTags)
                .register(meterRegistry);

        idSet.add(successfulCalls.getId());
        idSet.add(failedCalls.getId());
        idSet.add(ignoredFailedCalls.getId());
        idSet.add(notPermittedCalls.getId());

        // 注册并挂接消费者
        circuitBreaker.getEventPublisher()
                .onIgnoredError(event -> ignoredFailedCalls.record(event.getElapsedDuration()))
                .onCallNotPermitted(event -> notPermittedCalls.increment())
                .onSuccess(event -> successfulCalls.record(event.getElapsedDuration()))
                .onError(event -> failedCalls.record(event.getElapsedDuration()));

        meterIdMap.put(circuitBreaker.getName(), idSet);
    }


    @Deprecated
    public static class MetricNames extends CircuitBreakerMetricNames { }
}
