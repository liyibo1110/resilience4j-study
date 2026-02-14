package com.github.liyibo1110.resilience4j.timelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedTimeLimiterMetricsPublisher;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
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
 * @date 2026-02-14 11:38
 */
@Configuration
@ConditionalOnClass({MeterRegistry.class, TimeLimiter.class, TaggedTimeLimiterMetricsPublisher.class})
@AutoConfigureAfter({MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(value="resilience4j.timelimiter.metrics.enabled", matchIfMissing=true)
public class TimeLimiterMetricsAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value="resilience4j.timelimiter.metrics.legacy.enabled", havingValue="true")
    @ConditionalOnMissingBean
    public TaggedTimeLimiterMetrics registerTimeLimiterMetrics(TimeLimiterRegistry timeLimiterRegistry) {
        return TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(timeLimiterRegistry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(value="resilience4j.timelimiter.metrics.legacy.enabled", havingValue="false", matchIfMissing=true)
    @ConditionalOnMissingBean
    public TaggedTimeLimiterMetricsPublisher taggedTimeLimiterMetricsPublisher(MeterRegistry meterRegistry) {
        return new TaggedTimeLimiterMetricsPublisher(meterRegistry);
    }
}
