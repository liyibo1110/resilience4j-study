package com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.health;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-10 16:12
 */
public class CircuitBreakersHealthIndicator implements HealthIndicator {
    private static final String FAILURE_RATE = "failureRate";
    private static final String SLOW_CALL_RATE = "slowCallRate";
    private static final String FAILURE_RATE_THRESHOLD = "failureRateThreshold";
    private static final String SLOW_CALL_RATE_THRESHOLD = "slowCallRateThreshold";
    private static final String BUFFERED_CALLS = "bufferedCalls";
    private static final String FAILED_CALLS = "failedCalls";
    private static final String SLOW_CALLS = "slowCalls";
    private static final String SLOW_FAILED_CALLS = "slowFailedCalls";
    private static final String NOT_PERMITTED = "notPermittedCalls";
    private static final String STATE = "state";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final CircuitBreakerConfigurationProperties circuitBreakerProperties;
    private final StatusAggregator statusAggregator;

    public CircuitBreakersHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry,
                                          CircuitBreakerConfigurationProperties circuitBreakerProperties,
                                          StatusAggregator statusAggregator) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.circuitBreakerProperties = circuitBreakerProperties;
        this.statusAggregator = statusAggregator;
    }

    @Override
    public Health health() {
        Map<String, Health> healths = circuitBreakerRegistry.getAllCircuitBreakers().toJavaStream()
                .filter(this::isRegisterHealthIndicator)
                .collect(Collectors.toMap(CircuitBreaker::getName, this::mapBackendMonitorState));

        Status status = this.statusAggregator.getAggregateStatus(healths.values().stream().map(Health::getStatus).collect(Collectors.toSet()));
        return Health.status(status).withDetails(healths).build();
    }

    /**
     * 查看实例是否打开了healthIndicator开关
     */
    private boolean isRegisterHealthIndicator(CircuitBreaker circuitBreaker) {
        return this.circuitBreakerProperties.findCircuitBreakerProperties(circuitBreaker.getName())
                .map(com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties.InstanceProperties::getRegisterHealthIndicator)
                .orElse(false);
    }

    /**
     * 查看实例是否打开了allowHealthIndicatorToFail开关
     */
    private boolean allowHealthIndicatorToFail(CircuitBreaker circuitBreaker) {
        return circuitBreakerProperties.findCircuitBreakerProperties(circuitBreaker.getName())
                .map(com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties.InstanceProperties::getAllowHealthIndicatorToFail)
                .orElse(false);
    }

    /**
     * CircuitBreaker -> Health
     */
    private Health mapBackendMonitorState(CircuitBreaker circuitBreaker) {
        return switch(circuitBreaker.getState()) {
            case CLOSED -> addDetails(Health.up(), circuitBreaker).build();
            case OPEN -> {
                boolean allowHealthIndicatorToFail = allowHealthIndicatorToFail(circuitBreaker);
                yield addDetails(allowHealthIndicatorToFail ? Health.down() : Health.status("CIRCUIT_OPEN"), circuitBreaker).build();
            }
            case HALF_OPEN -> addDetails(Health.status("CIRCUIT_HALF_OPEN"), circuitBreaker).build();
            default -> addDetails(Health.unknown(), circuitBreaker).build();
        };
    }

    private static Health.Builder addDetails(Health.Builder builder, CircuitBreaker circuitBreaker) {
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();
        builder.withDetail(FAILURE_RATE, metrics.getFailureRate() + "%")
                .withDetail(FAILURE_RATE_THRESHOLD, config.getFailureRateThreshold() + "%")
                .withDetail(SLOW_CALL_RATE, metrics.getSlowCallRate() + "%")
                .withDetail(SLOW_CALL_RATE_THRESHOLD, config.getSlowCallRateThreshold() + "%")
                .withDetail(BUFFERED_CALLS, metrics.getNumberOfBufferedCalls())
                .withDetail(SLOW_CALLS, metrics.getNumberOfSlowCalls())
                .withDetail(SLOW_FAILED_CALLS, metrics.getNumberOfSlowFailedCalls())
                .withDetail(FAILED_CALLS, metrics.getNumberOfFailedCalls())
                .withDetail(NOT_PERMITTED, metrics.getNumberOfNotPermittedCalls())
                .withDetail(STATE, circuitBreaker.getState());
        return builder;
    }
}
