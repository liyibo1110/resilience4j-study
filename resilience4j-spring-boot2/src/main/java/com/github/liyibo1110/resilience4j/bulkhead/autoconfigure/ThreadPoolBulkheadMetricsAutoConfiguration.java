package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedThreadPoolBulkheadMetrics;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedThreadPoolBulkheadMetricsPublisher;
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
 * @date 2026-02-14 11:36
 */
@Configuration
@ConditionalOnClass({MeterRegistry.class, ThreadPoolBulkhead.class, TaggedThreadPoolBulkheadMetricsPublisher.class})
@AutoConfigureAfter({MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(value="resilience4j.thread-pool-bulkhead.metrics.enabled", matchIfMissing=true)
public class ThreadPoolBulkheadMetricsAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value="resilience4j.thread-pool-bulkhead.metrics.legacy.enabled", havingValue="true")
    @ConditionalOnMissingBean
    public TaggedThreadPoolBulkheadMetrics registerThreadPoolBulkheadMetrics(ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry) {
        return TaggedThreadPoolBulkheadMetrics.ofThreadPoolBulkheadRegistry(threadPoolBulkheadRegistry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(value="resilience4j.thread-pool-bulkhead.metrics.legacy.enabled", havingValue="false", matchIfMissing=true)
    @ConditionalOnMissingBean
    public TaggedThreadPoolBulkheadMetricsPublisher taggedThreadPoolBulkheadMetricsPublisher(MeterRegistry meterRegistry) {
        return new TaggedThreadPoolBulkheadMetricsPublisher(meterRegistry);
    }
}
