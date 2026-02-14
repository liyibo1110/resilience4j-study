package com.github.liyibo1110.resilience4j.micrometer.tagged;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-13 15:52
 */
public class ThreadPoolBulkheadMetricNames {
    private static final String DEFAULT_PREFIX = "resilience4j.bulkhead";
    public static final String DEFAULT_BULKHEAD_QUEUE_DEPTH_METRIC_NAME = DEFAULT_PREFIX + ".queue.depth";
    public static final String DEFAULT_BULKHEAD_QUEUE_CAPACITY_METRIC_NAME = DEFAULT_PREFIX + ".queue.capacity";
    public static final String DEFAULT_THREAD_POOL_SIZE_METRIC_NAME = DEFAULT_PREFIX + ".thread.pool.size";
    public static final String DEFAULT_MAX_THREAD_POOL_SIZE_METRIC_NAME = DEFAULT_PREFIX + ".max.thread.pool.size";
    public static final String DEFAULT_CORE_THREAD_POOL_SIZE_METRIC_NAME = DEFAULT_PREFIX + ".core.thread.pool.size";
    private String queueDepthMetricName = DEFAULT_BULKHEAD_QUEUE_DEPTH_METRIC_NAME;
    private String threadPoolSizeMetricName = DEFAULT_THREAD_POOL_SIZE_METRIC_NAME;
    private String maxThreadPoolSizeMetricName = DEFAULT_MAX_THREAD_POOL_SIZE_METRIC_NAME;
    private String coreThreadPoolSizeMetricName = DEFAULT_CORE_THREAD_POOL_SIZE_METRIC_NAME;
    private String queueCapacityMetricName = DEFAULT_BULKHEAD_QUEUE_CAPACITY_METRIC_NAME;

    protected ThreadPoolBulkheadMetricNames() {}

    public static Builder custom() {
        return new Builder();
    }

    public static ThreadPoolBulkheadMetricNames ofDefaults() {
        return new ThreadPoolBulkheadMetricNames();
    }

    public String getQueueDepthMetricName() {
        return queueDepthMetricName;
    }

    public String getThreadPoolSizeMetricName() {
        return threadPoolSizeMetricName;
    }

    public String getMaxThreadPoolSizeMetricName() {
        return maxThreadPoolSizeMetricName;
    }

    public String getCoreThreadPoolSizeMetricName() {
        return coreThreadPoolSizeMetricName;
    }

    public String getQueueCapacityMetricName() {
        return queueCapacityMetricName;
    }

    public static class Builder {
        private final ThreadPoolBulkheadMetricNames metricNames = new ThreadPoolBulkheadMetricNames();

        public Builder queueDepthMetricName(String queueDepthMetricName) {
            metricNames.queueDepthMetricName = requireNonNull(queueDepthMetricName);
            return this;
        }

        public Builder threadPoolSizeMetricName(String threadPoolSizeMetricName) {
            metricNames.threadPoolSizeMetricName =  requireNonNull(threadPoolSizeMetricName);
            return this;
        }

        public Builder maxThreadPoolSizeMetricName(String maxThreadPoolSizeMetricName) {
            metricNames.maxThreadPoolSizeMetricName = requireNonNull(
                    maxThreadPoolSizeMetricName);
            return this;
        }

        public Builder coreThreadPoolSizeMetricName(String coreThreadPoolSizeMetricName) {
            metricNames.coreThreadPoolSizeMetricName = requireNonNull(
                    coreThreadPoolSizeMetricName);
            return this;
        }

        public Builder queueCapacityMetricName(String queueCapacityMetricName) {
            metricNames.queueCapacityMetricName = requireNonNull(queueCapacityMetricName);
            return this;
        }

        public ThreadPoolBulkheadMetricNames build() {
            return metricNames;
        }
    }
}
