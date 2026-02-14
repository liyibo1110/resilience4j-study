package com.github.liyibo1110.resilience4j.micrometer.tagged;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-13 15:49
 */
public class TimeLimiterMetricNames {
    private static final String DEFAULT_PREFIX = "resilience4j.timelimiter";
    public static final String DEFAULT_TIME_LIMITER_CALLS = DEFAULT_PREFIX + ".calls";

    private String callsMetricName = DEFAULT_TIME_LIMITER_CALLS;

    public static Builder custom() {
        return new Builder();
    }

    public static TimeLimiterMetricNames ofDefaults() {
        return new TimeLimiterMetricNames();
    }

    public String getCallsMetricName() {
        return callsMetricName;
    }

    public static class Builder {
        private final TimeLimiterMetricNames metricNames = new TimeLimiterMetricNames();

        public Builder callsMetricName(String callsMetricName) {
            metricNames.callsMetricName = requireNonNull(callsMetricName);
            return this;
        }

        public TimeLimiterMetricNames build() {
            return metricNames;
        }
    }
}
