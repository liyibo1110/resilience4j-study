package com.github.liyibo1110.resilience4j.retry.autoconfigure;

import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedRetryMetricsPublisher;
import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
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
 * @date 2026-02-14 11:37
 */
@Configuration
@ConditionalOnClass({MeterRegistry.class, Retry.class, TaggedRetryMetricsPublisher.class})
@AutoConfigureAfter({MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(value="resilience4j.retry.metrics.enabled", matchIfMissing=true)
public class RetryMetricsAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value="resilience4j.retry.metrics.legacy.enabled", havingValue="true")
    @ConditionalOnMissingBean
    public TaggedRetryMetrics registerRetryMetrics(RetryRegistry retryRegistry) {
        return TaggedRetryMetrics.ofRetryRegistry(retryRegistry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(value="resilience4j.retry.metrics.legacy.enabled", havingValue="false", matchIfMissing=true)
    @ConditionalOnMissingBean
    public TaggedRetryMetricsPublisher taggedRetryMetricsPublisher(MeterRegistry meterRegistry) {
        return new TaggedRetryMetricsPublisher(meterRegistry);
    }
}
