package com.github.liyibo1110.resilience4j.core.metrics;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

/**
 * 基于一定时间的滑动窗口的Metrics实现，会聚合最近N秒调用。
 * 通过一个由n个聚合bucket组成的环形数组来实现的，如果时间窗口大小为10秒，则会有10个bucket。
 * 每个bucket发生在某个时间秒内的所有调用的结果（剩余细节和FixedSizeSlidingWindowMetrics是类似的）。
 * @author liyibo
 * @date 2026-02-04 23:06
 */
public class SlidingTimeWindowMetrics implements Metrics {
    /** 时间窗口，每个bucket代表这1秒间 */
    final PartialAggregation[] partialAggregations;

    /** bucket的总量（即最多要统计多少秒） */
    private final int timeWindowSizeInSeconds;

    /** 总统计器（注意是统计n秒的总和，不是无限累加） */
    private final TotalAggregation totalAggregation;

    private final Clock clock;

    /** 这一次要写入新统计值的bucket下标 */
    int headIndex;

    public SlidingTimeWindowMetrics(int timeWindowSizeInSeconds, Clock clock) {
        this.clock = clock;
        this.timeWindowSizeInSeconds = timeWindowSizeInSeconds;
        this.partialAggregations = new PartialAggregation[timeWindowSizeInSeconds];
        this.headIndex = 0;
        // 应该是秒单位的时间戳
        long epochSecond = clock.instant().getEpochSecond();
        for(int i = 0; i < timeWindowSizeInSeconds; i++) {
            partialAggregations[i] = new PartialAggregation(epochSecond);
            epochSecond++;
        }
        this.totalAggregation = new TotalAggregation();
    }

    @Override
    public Snapshot record(long duration, TimeUnit unit, Outcome outcome) {
        this.totalAggregation.record(duration, unit, outcome);
        this.moveWindowToCurrentEpochSecond(this.getLatestPartialAggregation()).record(duration, unit, outcome);
        return new SnapshotImpl(this.totalAggregation);
    }

    public synchronized Snapshot getSnapshot() {
        // 非常重要的调用，和基于次数的那个实现不同，这里因为时间是自动流逝的，所以当进入这个方法时，也要刷新一轮桶信息
        this.moveWindowToCurrentEpochSecond(getLatestPartialAggregation());
        return new SnapshotImpl(this.totalAggregation);
    }

    /**
     * 尝试移动一格窗口，并且尝试清理，并返回等待这一轮使用
     */
    private PartialAggregation moveWindowToCurrentEpochSecond(PartialAggregation latest) {
        long currentEpochSecond = clock.instant().getEpochSecond();
        long differenceInSeconds = currentEpochSecond - latest.getEpochSecond();
        // 如果还是在这1秒，则继续使用当前的bucket
        if(differenceInSeconds == 0)
            return latest;
        // 算出实际经过的秒数，如果超过总统计秒数了，则清空所有bucket
        long secondsToMoveTheWindow = Math.min(differenceInSeconds, this.timeWindowSizeInSeconds);
        PartialAggregation currentPartialAggregation;
        // 到这里已经至少过1秒了，先无条件清理1个bucket
        do {
            secondsToMoveTheWindow--;
            this.moveHeadIndexByOne();
            currentPartialAggregation = this.getLatestPartialAggregation();
            this.totalAggregation.removeBucket(currentPartialAggregation);
            // 非常重要同时稍微有点难以理解的计算，其实就是把特定桶，生成特定属于它那个秒值
            currentPartialAggregation.reset(currentEpochSecond - secondsToMoveTheWindow);
        } while(secondsToMoveTheWindow > 0);
        return currentPartialAggregation;
    }

    private PartialAggregation getLatestPartialAggregation() {
        return this.partialAggregations[this.headIndex];
    }

    void moveHeadIndexByOne() {
        this.headIndex = (this.headIndex + 1) % this.timeWindowSizeInSeconds;
    }
}
