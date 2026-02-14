package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedBulkheadMetricsPublisher;
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
 * @date 2026-02-14 11:24
 */
@Configuration
@ConditionalOnClass({MeterRegistry.class, Bulkhead.class, TaggedBulkheadMetricsPublisher.class})
@AutoConfigureAfter({MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(value="resilience4j.bulkhead.metrics.enabled", matchIfMissing=true)
public class BulkheadMetricsAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value="resilience4j.bulkhead.metrics.legacy.enabled", havingValue="true")
    @ConditionalOnMissingBean
    public TaggedBulkheadMetrics registerBulkheadMetrics(BulkheadRegistry bulkheadRegistry) {
        return TaggedBulkheadMetrics.ofBulkheadRegistry(bulkheadRegistry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(value="resilience4j.bulkhead.metrics.legacy.enabled", havingValue="false", matchIfMissing=true)
    @ConditionalOnMissingBean
    public TaggedBulkheadMetricsPublisher taggedBulkheadMetricsPublisher(MeterRegistry meterRegistry) {
        return new TaggedBulkheadMetricsPublisher(meterRegistry);
    }
}
