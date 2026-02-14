package com.github.liyibo1110.resilience4j.micrometer.tagged;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-13 15:46
 */
public class BulkheadMetricNames {
    private static final String DEFAULT_PREFIX = "resilience4j.bulkhead";

    public static final String DEFAULT_BULKHEAD_AVAILABLE_CONCURRENT_CALLS_METRIC_NAME = DEFAULT_PREFIX + ".available.concurrent.calls";
    public static final String DEFAULT_BULKHEAD_MAX_ALLOWED_CONCURRENT_CALLS_METRIC_NAME = DEFAULT_PREFIX + ".max.allowed.concurrent.calls";
    private String availableConcurrentCallsMetricName = DEFAULT_BULKHEAD_AVAILABLE_CONCURRENT_CALLS_METRIC_NAME;
    private String maxAllowedConcurrentCallsMetricName = DEFAULT_BULKHEAD_MAX_ALLOWED_CONCURRENT_CALLS_METRIC_NAME;

    protected BulkheadMetricNames() {}

    public static Builder custom() {
        return new Builder();
    }

    public static BulkheadMetricNames ofDefaults() {
        return new BulkheadMetricNames();
    }

    public String getAvailableConcurrentCallsMetricName() {
        return availableConcurrentCallsMetricName;
    }

    public String getMaxAllowedConcurrentCallsMetricName() {
        return maxAllowedConcurrentCallsMetricName;
    }

    public static class Builder {
        private final BulkheadMetricNames metricNames = new BulkheadMetricNames();

        public Builder availableConcurrentCallsMetricName(String availableConcurrentCallsMetricName) {
            metricNames.availableConcurrentCallsMetricName = requireNonNull(availableConcurrentCallsMetricName);
            return this;
        }

        public Builder maxAllowedConcurrentCallsMetricName(String maxAllowedConcurrentCallsMetricName) {
            metricNames.maxAllowedConcurrentCallsMetricName = requireNonNull(maxAllowedConcurrentCallsMetricName);
            return this;
        }

        public BulkheadMetricNames build() {
            return metricNames;
        }
    }
}
