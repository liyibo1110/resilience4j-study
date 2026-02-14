package com.github.liyibo1110.resilience4j.ratelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import com.github.liyibo1110.resilience4j.micrometer.tagged.TaggedRateLimiterMetricsPublisher;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
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
 * @date 2026-02-14 11:35
 */
@Configuration
@ConditionalOnClass({MeterRegistry.class, RateLimiter.class, TaggedRateLimiterMetricsPublisher.class})
@AutoConfigureAfter({MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(value="resilience4j.ratelimiter.metrics.enabled", matchIfMissing=true)
public class RateLimiterMetricsAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value="resilience4j.ratelimiter.metrics.legacy.enabled", havingValue="true")
    @ConditionalOnMissingBean
    public TaggedRateLimiterMetrics registerRateLimiterMetrics(RateLimiterRegistry rateLimiterRegistry) {
        return TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(value="resilience4j.ratelimiter.metrics.legacy.enabled", havingValue="false", matchIfMissing=true)
    @ConditionalOnMissingBean
    public TaggedRateLimiterMetricsPublisher taggedRateLimiterMetricsPublisher(MeterRegistry meterRegistry) {
        return new TaggedRateLimiterMetricsPublisher(meterRegistry);
    }
}
