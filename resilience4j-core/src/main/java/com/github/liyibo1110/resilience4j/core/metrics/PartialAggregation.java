package com.github.liyibo1110.resilience4j.core.metrics;

/**
 * 提供了基于时间窗内分桶/聚合的AbstractAggregation扩展，
 * 就是会周期性调用reset来清空自己的统计值，来进行下一时间周期的统计。
 * @author liyibo
 * @date 2026-02-04 18:12
 */
public class PartialAggregation extends AbstractAggregation {

    private long epochSecond;

    PartialAggregation(long epochSecond) {
        this.epochSecond = epochSecond;
    }

    void reset(long epochSecond) {
        this.epochSecond = epochSecond;
        this.totalDurationInMillis = 0;
        this.numberOfSlowCalls = 0;
        this.numberOfFailedCalls = 0;
        this.numberOfSlowFailedCalls = 0;
        this.numberOfCalls = 0;
    }

    public long getEpochSecond() {
        return epochSecond;
    }
}
