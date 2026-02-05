package com.github.liyibo1110.resilience4j.core.metrics;

/**
 * AbstractAggregation的扩展，增加了removeBucket
 * @author liyibo
 * @date 2026-02-04 18:08
 */
class TotalAggregation extends AbstractAggregation {
    /**
     * 减少对应的各统计值
     */
    void removeBucket(AbstractAggregation bucket) {
        this.totalDurationInMillis -= bucket.totalDurationInMillis;
        this.numberOfSlowCalls -= bucket.numberOfSlowCalls;
        this.numberOfSlowFailedCalls -= bucket.numberOfSlowFailedCalls;
        this.numberOfFailedCalls -= bucket.numberOfFailedCalls;
        this.numberOfCalls -= bucket.numberOfCalls;
    }
}
