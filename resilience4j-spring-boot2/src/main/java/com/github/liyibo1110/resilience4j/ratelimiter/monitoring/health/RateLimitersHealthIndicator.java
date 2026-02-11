package com.github.liyibo1110.resilience4j.ratelimiter.monitoring.health;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-10 16:48
 */
public class RateLimitersHealthIndicator implements HealthIndicator {
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiterConfigurationProperties rateLimiterProperties;
    private final StatusAggregator statusAggregator;

    public RateLimitersHealthIndicator(RateLimiterRegistry rateLimiterRegistry,
                                       RateLimiterConfigurationProperties rateLimiterProperties,
                                       StatusAggregator statusAggregator) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.rateLimiterProperties = rateLimiterProperties;
        this.statusAggregator = statusAggregator;
    }

    @Override
    public Health health() {
        Map<String, Health> healths = rateLimiterRegistry.getAllRateLimiters().toJavaStream()
                .filter(this::isRegisterHealthIndicator)
                .collect(Collectors.toMap(RateLimiter::getName, this::mapRateLimiterHealth));
        Status status = statusAggregator.getAggregateStatus(healths.values().stream().map(Health::getStatus).collect(Collectors.toSet()));
        return Health.status(status).withDetails(healths).build();
    }

    private boolean isRegisterHealthIndicator(RateLimiter rateLimiter) {
        return rateLimiterProperties.findRateLimiterProperties(rateLimiter.getName())
                .map(com.github.liyibo1110.resilience4j.common.ratelimiter.configuration.RateLimiterConfigurationProperties.InstanceProperties::getRegisterHealthIndicator)
                .orElse(false);
    }

    private boolean allowHealthIndicatorToFail(RateLimiter rateLimiter) {
        return rateLimiterProperties.findRateLimiterProperties(rateLimiter.getName())
                .map(com.github.liyibo1110.resilience4j.common.ratelimiter.configuration.RateLimiterConfigurationProperties.InstanceProperties::getAllowHealthIndicatorToFail)
                .orElse(false);
    }

    private Health mapRateLimiterHealth(RateLimiter rateLimiter) {
        RateLimiter.Metrics metrics = rateLimiter.getMetrics();
        int availablePermissions = metrics.getAvailablePermissions();
        int numberOfWaitingThreads = metrics.getNumberOfWaitingThreads();
        long timeoutInNanos = rateLimiter.getRateLimiterConfig().getTimeoutDuration().toNanos();

        if(availablePermissions > 0 || numberOfWaitingThreads == 0)
            return rateLimiterHealth(Status.UP, availablePermissions, numberOfWaitingThreads);

        if(rateLimiter instanceof AtomicRateLimiter) {
            AtomicRateLimiter atomicRateLimiter = (AtomicRateLimiter) rateLimiter;
            AtomicRateLimiter.AtomicRateLimiterMetrics detailedMetrics = atomicRateLimiter.getDetailedMetrics();
            if (detailedMetrics.getNanosToWait() > timeoutInNanos) {
                boolean allowHealthIndicatorToFail = allowHealthIndicatorToFail(rateLimiter);
                return rateLimiterHealth(allowHealthIndicatorToFail ? Status.DOWN : new Status("RATE_LIMITED"), availablePermissions, numberOfWaitingThreads);
            }
        }
        return rateLimiterHealth(Status.UNKNOWN, availablePermissions, numberOfWaitingThreads);
    }

    private static Health rateLimiterHealth(Status status, int availablePermissions, int numberOfWaitingThreads) {
        return Health.status(status)
                .withDetail("availablePermissions", availablePermissions)
                .withDetail("numberOfWaitingThreads", numberOfWaitingThreads)
                .build();
    }
}
