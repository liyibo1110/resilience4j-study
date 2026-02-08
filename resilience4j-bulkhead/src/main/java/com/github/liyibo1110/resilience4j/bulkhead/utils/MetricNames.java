package com.github.liyibo1110.resilience4j.bulkhead.utils;

/**
 * @author liyibo
 * @date 2026-02-08 20:23
 */
public class MetricNames {

    public static final String DEFAULT_PREFIX = "resilience4j.bulkhead";
    public static final String AVAILABLE_CONCURRENT_CALLS = "available_concurrent_calls";
    public static final String MAX_ALLOWED_CONCURRENT_CALLS = "max_allowed_concurrent_calls";
    public static final String DEFAULT_PREFIX_THREAD_POOL = "resilience4j.thread_pool_bulkhead";
    public static final String CURRENT_THREAD_POOL_SIZE = "current_thread_pool_size";
    public static final String AVAILABLE_QUEUE_CAPACITY = "available_queue_capacity";

    private MetricNames() {}
}
