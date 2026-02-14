package com.github.liyibo1110.resilience4j.micrometer.tagged;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import static java.util.Objects.requireNonNull;

/**
 * 基于MeterBinder实现（前缀Tagged意思就是基于tag标签分类的指标），风格属于批量绑定（即legacy传统模式），
 * 会在项目启动时，通过扫描registry来把所有cb指标注册到micrometer，后来新增的cb也能支持自动注册（后面又推出了TaggedCircuitBreakerMetricsPublisher，功能其实差不多，实现原理不同）。
 * 最终这个类生成出来的bean，会由Spring Boot自动调用其bindTo()方法
 * @author liyibo
 * @date 2026-02-14 10:14
 */
public class TaggedCircuitBreakerMetrics extends AbstractCircuitBreakerMetrics implements MeterBinder {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private TaggedCircuitBreakerMetrics(CircuitBreakerMetricNames names, CircuitBreakerRegistry circuitBreakerRegistry) {
        super(names);
        this.circuitBreakerRegistry = requireNonNull(circuitBreakerRegistry);
    }

    public static TaggedCircuitBreakerMetrics ofCircuitBreakerRegistry(CircuitBreakerRegistry circuitBreakerRegistry) {
        return new TaggedCircuitBreakerMetrics(CircuitBreakerMetricNames.ofDefaults(), circuitBreakerRegistry);
    }

    public static TaggedCircuitBreakerMetrics ofCircuitBreakerRegistry(CircuitBreakerMetricNames circuitBreakerMetricNames,
                                                                       CircuitBreakerRegistry circuitBreakerRegistry) {
        return new TaggedCircuitBreakerMetrics(circuitBreakerMetricNames, circuitBreakerRegistry);
    }

    @Deprecated
    public static TaggedCircuitBreakerMetrics ofCircuitBreakerRegistry(MetricNames circuitBreakerMetricNames,
                                                                       CircuitBreakerRegistry circuitBreakerRegistry) {
        return new TaggedCircuitBreakerMetrics(circuitBreakerMetricNames, circuitBreakerRegistry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        // 在这里进行注册
        for(CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers())
            addMetrics(registry, circuitBreaker);
        // 在这里监听cb实例的新增/修改/删除，再联动内部registerMetrics方法的执行，完成整个指标对接
        circuitBreakerRegistry.getEventPublisher().onEntryAdded(event -> addMetrics(registry, event.getAddedEntry()));
        circuitBreakerRegistry.getEventPublisher().onEntryRemoved(event -> removeMetrics(registry, event.getRemovedEntry().getName()));
        circuitBreakerRegistry.getEventPublisher().onEntryReplaced(event -> {
            removeMetrics(registry, event.getOldEntry().getName());
            addMetrics(registry, event.getNewEntry());
        });
    }
}
