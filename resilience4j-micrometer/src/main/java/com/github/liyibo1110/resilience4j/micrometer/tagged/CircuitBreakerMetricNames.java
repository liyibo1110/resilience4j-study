package com.github.liyibo1110.resilience4j.micrometer.tagged;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-13 15:44
 */
public class CircuitBreakerMetricNames {
    private static final String DEFAULT_PREFIX = "resilience4j.circuitbreaker";
    public static final String DEFAULT_CIRCUIT_BREAKER_CALLS = DEFAULT_PREFIX + ".calls";
    public static final String DEFAULT_CIRCUIT_BREAKER_NOT_PERMITTED_CALLS = DEFAULT_PREFIX + ".not.permitted.calls";
    public static final String DEFAULT_CIRCUIT_BREAKER_STATE = DEFAULT_PREFIX + ".state";
    public static final String DEFAULT_CIRCUIT_BREAKER_BUFFERED_CALLS =
            DEFAULT_PREFIX + ".buffered.calls";
    public static final String DEFAULT_CIRCUIT_BREAKER_SLOW_CALLS =
            DEFAULT_PREFIX + ".slow.calls";
    public static final String DEFAULT_CIRCUIT_BREAKER_FAILURE_RATE =
            DEFAULT_PREFIX + ".failure.rate";
    public static final String DEFAULT_CIRCUIT_BREAKER_SLOW_CALL_RATE =
            DEFAULT_PREFIX + ".slow.call.rate";

    private String callsMetricName = DEFAULT_CIRCUIT_BREAKER_CALLS;
    private String notPermittedCallsMetricName = DEFAULT_CIRCUIT_BREAKER_NOT_PERMITTED_CALLS;
    private String stateMetricName = DEFAULT_CIRCUIT_BREAKER_STATE;
    private String bufferedCallsMetricName = DEFAULT_CIRCUIT_BREAKER_BUFFERED_CALLS;
    private String slowCallsMetricName = DEFAULT_CIRCUIT_BREAKER_SLOW_CALLS;
    private String failureRateMetricName = DEFAULT_CIRCUIT_BREAKER_FAILURE_RATE;
    private String slowCallRateMetricName = DEFAULT_CIRCUIT_BREAKER_SLOW_CALL_RATE;

    protected CircuitBreakerMetricNames() {}

    public static Builder custom() {
        return new Builder();
    }

    public static CircuitBreakerMetricNames ofDefaults() {
        return new CircuitBreakerMetricNames();
    }

    public String getCallsMetricName() {
        return callsMetricName;
    }

    public String getNotPermittedCallsMetricName() {
        return notPermittedCallsMetricName;
    }

    public String getBufferedCallsMetricName() {
        return bufferedCallsMetricName;
    }

    public String getSlowCallsMetricName() {
        return slowCallsMetricName;
    }

    public String getStateMetricName() {
        return stateMetricName;
    }

    public String getFailureRateMetricName() {
        return failureRateMetricName;
    }

    public String getSlowCallRateMetricName() {
        return slowCallRateMetricName;
    }

    public static class Builder {
        private final CircuitBreakerMetricNames metricNames = new CircuitBreakerMetricNames();

        public Builder callsMetricName(String callsMetricName) {
            metricNames.callsMetricName = requireNonNull(callsMetricName);
            return this;
        }

        public Builder notPermittedCallsMetricName(String notPermittedCallsMetricName) {
            metricNames.notPermittedCallsMetricName = requireNonNull(notPermittedCallsMetricName);
            return this;
        }

        public Builder stateMetricName(String stateMetricName) {
            metricNames.stateMetricName = requireNonNull(stateMetricName);
            return this;
        }

        public Builder bufferedCallsMetricName(String bufferedCallsMetricName) {
            metricNames.bufferedCallsMetricName = requireNonNull(bufferedCallsMetricName);
            return this;
        }

        public Builder slowCallsMetricName(String slowCallsMetricName) {
            metricNames.slowCallsMetricName = requireNonNull(slowCallsMetricName);
            return this;
        }

        public Builder failureRateMetricName(String failureRateMetricName) {
            metricNames.failureRateMetricName = requireNonNull(failureRateMetricName);
            return this;
        }

        public Builder slowCallRateMetricName(String slowCallRateMetricName) {
            metricNames.slowCallRateMetricName = requireNonNull(slowCallRateMetricName);
            return this;
        }

        public CircuitBreakerMetricNames build() {
            return metricNames;
        }
    }
}
