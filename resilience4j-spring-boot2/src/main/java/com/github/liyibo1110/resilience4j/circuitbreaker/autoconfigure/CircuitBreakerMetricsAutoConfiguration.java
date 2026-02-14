package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetricsPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-14 11:20
 */
@Configuration
@ConditionalOnClass({MeterRegistry.class, CircuitBreaker.class, TaggedCircuitBreakerMetricsPublisher.class})
@AutoConfigureAfter({MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(value="resilience4j.circuitbreaker.metrics.enabled", matchIfMissing=true)
public class CircuitBreakerMetricsAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value="resilience4j.circuitbreaker.metrics.legacy.enabled", havingValue="true")
    @ConditionalOnMissingBean
    public TaggedCircuitBreakerMetrics registerCircuitBreakerMetrics(CircuitBreakerRegistry circuitBreakerRegistry) {
        return TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(value="resilience4j.circuitbreaker.metrics.legacy.enabled", havingValue="false", matchIfMissing=true)
    @ConditionalOnMissingBean
    public TaggedCircuitBreakerMetricsPublisher taggedCircuitBreakerMetricsPublisher(MeterRegistry meterRegistry) {
        return new TaggedCircuitBreakerMetricsPublisher(meterRegistry);
    }
}
