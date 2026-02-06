package com.github.liyibo1110.resilience4j.ratelimiter.utils;

/**
 * @author liyibo
 * @date 2026-02-06 16:04
 */
public class MetricNames {
    public static final String DEFAULT_PREFIX = "resilience4j.ratelimiter";

    /** 排队等候的线程数 */
    public static final String WAITING_THREADS = "number_of_waiting_threads";

    /** 当前可用的许可数 */
    public static final String AVAILABLE_PERMISSIONS = "available_permissions";
}
