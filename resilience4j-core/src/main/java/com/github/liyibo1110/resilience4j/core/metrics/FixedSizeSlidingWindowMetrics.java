package com.github.liyibo1110.resilience4j.core.metrics;

import java.util.concurrent.TimeUnit;

/**
 * 基于固定大小的滑动窗口的Metrics实现，会聚合最近N次调用。
 * 滑动窗口通过一个包含N个测量值的数组来实现的，假如时间窗口大小为10，则数组中始终包含10个测量值。
 * 以增量方式更新总聚合值，当record新的结果时，总聚合值会进行增量更新。
 * 当最旧的测量值被移除时，测量值会从总测量值减去。
 * 由于snapshot时预先聚合的，并且与窗口大小无关，因此检索snapshot的时间是O(1)，空间需求是O(n)。
 * @author liyibo
 * @date 2026-02-04 21:41
 */
public class FixedSizeSlidingWindowMetrics implements Metrics {
    /** 窗口大小 */
    private final int windowSize;

    /** 总统计器（注意是统计windowSize次数的总和，不是无限累加） */
    private final TotalAggregation totalAggregation;

    /** 环形数组（bucket），新值会顶替最老的值，每个Measurement就是这个bucket的累加统计 */
    private final Measurement[] measurements;

    /** 这一次要写入新统计值的bucket下标 */
    int headIndex;

    public FixedSizeSlidingWindowMetrics(int windowSize) {
        this.windowSize = windowSize;
        this.measurements = new Measurement[this.windowSize];
        for(int i = 0; i < this.windowSize; i++)
            measurements[i] = new Measurement();
        this.headIndex = 0;
        this.totalAggregation = new TotalAggregation();
    }

    @Override
    public Snapshot record(long duration, TimeUnit unit, Outcome outcome) {
        // 先计入总统计器
        this.totalAggregation.record(duration, unit, outcome);
        // 切换到下一个bucket，清理后记录新统计
        this.moveWindowByOne().record(duration, unit, outcome);
        return new SnapshotImpl(this.totalAggregation);
    }

    public synchronized Snapshot getSnapshot() {
        return new SnapshotImpl(this.totalAggregation);
    }

    /**
     * 移动一格窗口，并且尝试清理，并返回等待这一轮使用
     */
    private Measurement moveWindowByOne() {
        this.moveHeadIndexByOne();  // 向右移动一格index
        /**
         * 取出这个桶，它有2种可能：一种的桶还没被用过，内部统计都是0，二种是桶在上一圈被用过，里面已经有统计值了，
         * 但不管有没有，直接当作有处理就完了（没用过的桶统计反正都是0，总统计减去0也没有关系）
         */
        Measurement bucket = this.getLatestMeasurement();
        this.totalAggregation.removeBucket(bucket);
        bucket.reset();
        return bucket;
    }

    /**
     * 获取循环数组的第1个测量值。
     */
    private Measurement getLatestMeasurement() {
        return this.measurements[this.headIndex];
    }

    /**
     * 移动index，如果指向的是最后1个元素，则移到数组第1个元素。
     */
    void moveHeadIndexByOne() {
        this.headIndex = (headIndex + 1) % windowSize;
    }
}
