package com.github.liyibo1110.resilience4j.micrometer.tagged;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-13 15:47
 */
public class RateLimiterMetricNames {
    private static final String DEFAULT_PREFIX = "resilience4j.ratelimiter";

    public static final String DEFAULT_AVAILABLE_PERMISSIONS_METRIC_NAME = DEFAULT_PREFIX + ".available.permissions";
    public static final String DEFAULT_WAITING_THREADS_METRIC_NAME = DEFAULT_PREFIX + ".waiting_threads";
    private String availablePermissionsMetricName = DEFAULT_AVAILABLE_PERMISSIONS_METRIC_NAME;
    private String waitingThreadsMetricName = DEFAULT_WAITING_THREADS_METRIC_NAME;

    public static Builder custom() {
        return new Builder();
    }

    public static RateLimiterMetricNames ofDefaults() {
        return new RateLimiterMetricNames();
    }

    public String getAvailablePermissionsMetricName() {
        return availablePermissionsMetricName;
    }

    public String getWaitingThreadsMetricName() {
        return waitingThreadsMetricName;
    }

    public static class Builder {
        private final RateLimiterMetricNames metricNames = new RateLimiterMetricNames();

        public Builder availablePermissionsMetricName(String availablePermissionsMetricName) {
            metricNames.availablePermissionsMetricName = requireNonNull(
                    availablePermissionsMetricName);
            return this;
        }

        public Builder waitingThreadsMetricName(String waitingThreadsMetricName) {
            metricNames.waitingThreadsMetricName = requireNonNull(waitingThreadsMetricName);
            return this;
        }

        public RateLimiterMetricNames build() {
            return metricNames;
        }
    }
}
