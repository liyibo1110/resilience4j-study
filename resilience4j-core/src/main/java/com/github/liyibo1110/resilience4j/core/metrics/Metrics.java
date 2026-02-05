package com.github.liyibo1110.resilience4j.core.metrics;

import java.util.concurrent.TimeUnit;

/**
 * @author liyibo
 * @date 2026-02-04 18:02
 */
public interface Metrics {

    /**
     * 给定call时长和call结果类型，生成Snapshot实例
     */
    Snapshot record(long duration, TimeUnit unit, Outcome outcome);

    Snapshot getSnapshot();

    /**
     * 统计结果类型
     */
    enum Outcome {
        SUCCESS, ERROR, SLOW_SUCCESS, SLOW_ERROR
    }
}
