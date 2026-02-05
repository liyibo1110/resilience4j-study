package com.github.liyibo1110.resilience4j.core.metrics;

import java.util.concurrent.TimeUnit;

/**
 * 统计值聚合器的骨架
 * @author liyibo
 * @date 2026-02-04 18:06
 */
class AbstractAggregation {
    long totalDurationInMillis = 0;
    int numberOfSlowCalls = 0;
    int numberOfSlowFailedCalls = 0;
    int numberOfFailedCalls = 0;
    int numberOfCalls = 0;

    /**
     * 记录新的call用时
     */
    void record(long duration, TimeUnit unit, Metrics.Outcome outcome) {
        this.numberOfCalls++;
        this.totalDurationInMillis += unit.toMillis(duration);
        switch(outcome) {
            case SLOW_SUCCESS -> this.numberOfSlowCalls++;
            case SLOW_ERROR -> {
                numberOfSlowCalls++;
                numberOfFailedCalls++;
                numberOfSlowFailedCalls++;
            }
            case ERROR -> numberOfFailedCalls++;
        }
    }
}
