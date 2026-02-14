package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.core.metrics.MetricsPublisher;
import io.micrometer.core.instrument.MeterRegistry;

import static java.util.Objects.requireNonNull;

/**
 * 基于MetricsPublish实现，风格属于事件驱动（新模式），
 * 会在通过CircuitBreakerRegistry创建cb实例时再触发，是后面新版本又推出的功能。
 * 和TaggedCircuitBreakerMetrics属于独立并行的2套指标发布组件
 * @author liyibo
 * @date 2026-02-14 10:30
 */
public class TaggedCircuitBreakerMetricsPublisher extends AbstractCircuitBreakerMetrics implements MetricsPublisher<CircuitBreaker> {
    private final MeterRegistry meterRegistry;

    public TaggedCircuitBreakerMetricsPublisher(MeterRegistry meterRegistry) {
        super(CircuitBreakerMetricNames.ofDefaults());
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    public TaggedCircuitBreakerMetricsPublisher(CircuitBreakerMetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Deprecated
    public TaggedCircuitBreakerMetricsPublisher(MetricNames names, MeterRegistry meterRegistry) {
        super(names);
        this.meterRegistry = requireNonNull(meterRegistry);
    }

    @Override
    public void publishMetrics(CircuitBreaker entry) {
        addMetrics(meterRegistry, entry);
    }

    @Override
    public void removeMetrics(CircuitBreaker entry) {
        removeMetrics(meterRegistry, entry.getName());
    }
}
