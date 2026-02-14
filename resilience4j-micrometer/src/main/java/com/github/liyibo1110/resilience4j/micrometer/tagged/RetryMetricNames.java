package com.github.liyibo1110.resilience4j.micrometer.tagged;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-13 15:48
 */
public class RetryMetricNames {
    public static final String DEFAULT_RETRY_CALLS = "resilience4j.retry.calls";

    public static Builder custom() {
        return new Builder();
    }

    public static RetryMetricNames ofDefaults() {
        return new RetryMetricNames();
    }

    private String callsMetricName = DEFAULT_RETRY_CALLS;

    protected RetryMetricNames() {}

    public String getCallsMetricName() {
        return callsMetricName;
    }

    public static class Builder {
        private final RetryMetricNames retryMetricNames = new RetryMetricNames();

        public Builder callsMetricName(String callsMetricName) {
            retryMetricNames.callsMetricName = requireNonNull(callsMetricName);
            return this;
        }

        public RetryMetricNames build() {
            return retryMetricNames;
        }
    }
}
