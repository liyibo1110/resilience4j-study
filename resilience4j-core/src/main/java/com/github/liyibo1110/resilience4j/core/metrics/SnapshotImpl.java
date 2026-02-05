package com.github.liyibo1110.resilience4j.core.metrics;

import java.time.Duration;

/**
 * Snapshot的默认实现
 * @author liyibo
 * @date 2026-02-04 18:05
 */
public class SnapshotImpl implements Snapshot {

    private final long totalDurationInMillis;
    private final int totalNumberOfSlowCalls;
    private final int totalNumberOfSlowFailedCalls;
    private final int totalNumberOfFailedCalls;
    private final int totalNumberOfCalls;

    SnapshotImpl(TotalAggregation totalAggregation) {
        this.totalDurationInMillis = totalAggregation.totalDurationInMillis;
        this.totalNumberOfSlowCalls = totalAggregation.numberOfSlowCalls;
        this.totalNumberOfSlowFailedCalls = totalAggregation.numberOfSlowFailedCalls;
        this.totalNumberOfFailedCalls = totalAggregation.numberOfFailedCalls;
        this.totalNumberOfCalls = totalAggregation.numberOfCalls;
    }

    @Override
    public Duration getTotalDuration() {
        return Duration.ofMillis(totalDurationInMillis);
    }

    @Override
    public int getTotalNumberOfSlowCalls() {
        return totalNumberOfSlowCalls;
    }

    @Override
    public int getNumberOfSlowSuccessfulCalls() {
        return totalNumberOfSlowCalls - totalNumberOfSlowFailedCalls;
    }

    @Override
    public int getNumberOfSlowFailedCalls() {
        return totalNumberOfSlowFailedCalls;
    }

    @Override
    public float getSlowCallRate() {
        if(totalNumberOfCalls == 0)
            return 0;
        return totalNumberOfSlowCalls * 100.0f / totalNumberOfCalls;
    }

    @Override
    public int getNumberOfSuccessfulCalls() {
        return totalNumberOfCalls - totalNumberOfFailedCalls;
    }

    @Override
    public int getNumberOfFailedCalls() {
        return totalNumberOfFailedCalls;
    }

    @Override
    public int getTotalNumberOfCalls() {
        return totalNumberOfCalls;
    }

    @Override
    public float getFailureRate() {
        if(totalNumberOfCalls == 0)
            return 0;
        return totalNumberOfFailedCalls * 100.0f / totalNumberOfCalls;
    }

    @Override
    public Duration getAverageDuration() {
        if(totalNumberOfCalls == 0)
            return Duration.ZERO;
        return Duration.ofMillis(totalDurationInMillis / totalNumberOfCalls);
    }
}
