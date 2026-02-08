package com.github.liyibo1110.resilience4j.timelimiter.utils;

/**
 * @author liyibo
 * @date 2026-02-08 18:19
 */
public class MetricNames {
    public static final String DEFAULT_PREFIX = "resilience4j.timelimiter";
    public static final String SUCCESSFUL = "successful";
    public static final String FAILED = "failed";
    public static final String TIMEOUT = "timeout";
    public static final String PREFIX_NULL = "Prefix must not be null";
    public static final String ITERABLE_NULL = "TimeLimiters iterable must not be null";

    private MetricNames() {}
}
