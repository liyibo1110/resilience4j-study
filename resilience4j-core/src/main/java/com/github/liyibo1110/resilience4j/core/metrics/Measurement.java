package com.github.liyibo1110.resilience4j.core.metrics;

/**
 * AbstractAggregation的扩展，增加了reset
 * @author liyibo
 * @date 2026-02-04 21:34
 */
class Measurement extends AbstractAggregation{

    void reset() {
        this.totalDurationInMillis = 0;
        this.numberOfSlowCalls = 0;
        this.numberOfFailedCalls = 0;
        this.numberOfSlowFailedCalls = 0;
        this.numberOfCalls = 0;
    }
}
