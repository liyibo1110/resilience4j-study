package com.github.liyibo1110.resilience4j.circuitbreaker.internal;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.core.metrics.FixedSizeSlidingWindowMetrics;
import com.github.liyibo1110.resilience4j.core.metrics.Metrics;
import com.github.liyibo1110.resilience4j.core.metrics.SlidingTimeWindowMetrics;
import com.github.liyibo1110.resilience4j.core.metrics.Snapshot;

import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 根据滑动窗口的统计，来判断是否到达了熔断阈值（所以这不是一个仅负责事后统计的彩蛋组件，它还参与了熔断条件的判断）。
 * 核心指标为：failureRate（失败调用 / 总调用）和slowCallRate（慢调用 / 总调用）
 * 同时根据cb的状态，会动态切换到相应的metrics实现（就是forClosed、forHalfOpen等这些方法负责干的事儿）。
 * @author liyibo
 * @date 2026-02-05 16:42
 */
public class CircuitBreakerMetrics implements CircuitBreaker.Metrics {
    /** 注意这里才是core模块里面的通用Metrics */
    private final Metrics metrics;

    /** 熔断指标1： */
    private final float failureRateThreshold;
    private final float slowCallRateThreshold;
    private final long slowCallDurationThresholdInNanos;
    private final LongAdder numberOfNotPermittedCalls;
    private int minimumNumberOfCalls;

    private CircuitBreakerMetrics(int slidingWindowSize, CircuitBreakerConfig.SlidingWindowType slidingWindowType,
                                  CircuitBreakerConfig config, Clock clock) {
        if(slidingWindowType == CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) {
            this.metrics = new FixedSizeSlidingWindowMetrics(slidingWindowSize);
            this.minimumNumberOfCalls = Math.min(config.getMinimumNumberOfCalls(), slidingWindowSize);
        }else {
            this.metrics = new SlidingTimeWindowMetrics(slidingWindowSize, clock);
            this.minimumNumberOfCalls = config.getMinimumNumberOfCalls();
        }
        this.failureRateThreshold = config.getFailureRateThreshold();
        this.slowCallRateThreshold = config.getSlowCallRateThreshold();
        this.slowCallDurationThresholdInNanos = config.getSlowCallDurationThreshold().toNanos();
        this.numberOfNotPermittedCalls = new LongAdder();
    }

    private CircuitBreakerMetrics(int slidingWindowSize, CircuitBreakerConfig config, Clock clock) {
        this(slidingWindowSize, config.getSlidingWindowType(), config, clock);
    }

    /**
     * closed状态时要使用的metric策略（大窗口，用于长期健康度评估）
     */
    static CircuitBreakerMetrics forClosed(CircuitBreakerConfig config, Clock clock) {
        return new CircuitBreakerMetrics(config.getSlidingWindowSize(), config, clock);
    }

    /**
     * halfOpen状态时要使用的metric策略（小窗口，用于短期探测成功率）
     */
    static CircuitBreakerMetrics forHalfOpen(int permittedNumberOfCallsInHalfOpenState, CircuitBreakerConfig config, Clock clock) {
        return new CircuitBreakerMetrics(permittedNumberOfCallsInHalfOpenState,
                CircuitBreakerConfig.SlidingWindowType.COUNT_BASED, config, clock);
    }

    /**
     * forcedOpen状态时要使用的metric策略（零窗口，不需要统计了）
     */
    static CircuitBreakerMetrics forForcedOpen(CircuitBreakerConfig config, Clock clock) {
        return new CircuitBreakerMetrics(0, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED, config, clock);
    }

    /**
     * disabled状态时要使用的metric策略（零窗口，不需要统计了）
     */
    static CircuitBreakerMetrics forDisabled(CircuitBreakerConfig config, Clock clock) {
        return new CircuitBreakerMetrics(0, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED, config, clock);
    }

    /**
     * metricsOnly状态时要使用的metric策略（大窗口，用于长期健康度评估）
     */
    static CircuitBreakerMetrics forMetricsOnly(CircuitBreakerConfig config, Clock clock) {
        return forClosed(config, clock);
    }

    void onCallNotPermitted() {
        numberOfNotPermittedCalls.increment();
    }

    public Result onSuccess(long duration, TimeUnit unit) {
        Snapshot snapshot;
        if(unit.toNanos(duration) > slowCallDurationThresholdInNanos)
            snapshot = metrics.record(duration, unit, Metrics.Outcome.SLOW_SUCCESS);
        else
            snapshot = metrics.record(duration, unit, Metrics.Outcome.SUCCESS);
        return this.checkIfThresholdsExceeded(snapshot);
    }

    public Result onError(long duration, TimeUnit unit) {
        Snapshot snapshot;
        if(unit.toNanos(duration) > slowCallDurationThresholdInNanos)
            snapshot = metrics.record(duration, unit, Metrics.Outcome.SLOW_ERROR);
        else
            snapshot = metrics.record(duration, unit, Metrics.Outcome.ERROR);
        return this.checkIfThresholdsExceeded(snapshot);
    }

    /**
     * 根据给定snapshot统计，来判断当前的健康结果
     */
    private Result checkIfThresholdsExceeded(Snapshot snapshot) {
        float failureRateInPercentage = this.getFailureRate(snapshot);
        float slowCallsInPercentage = this.getSlowCallRate(snapshot);

        if(failureRateInPercentage == -1 || slowCallsInPercentage == -1)
            return Result.BELOW_MINIMUM_CALLS_THRESHOLD;

        if(failureRateInPercentage >= failureRateThreshold && slowCallsInPercentage >= slowCallRateThreshold)
            return Result.ABOVE_THRESHOLDS;

        if(failureRateInPercentage >= failureRateThreshold)
            return Result.FAILURE_RATE_ABOVE_THRESHOLDS;

        if(slowCallsInPercentage >= slowCallRateThreshold)
            return Result.SLOW_CALL_RATE_ABOVE_THRESHOLDS;

        return Result.BELOW_THRESHOLDS;
    }

    /**
     * 计算失败率
     */
    public float getFailureRate(Snapshot snapshot) {
        // 先检测样本是否足够了
        int bufferedCalls = snapshot.getTotalNumberOfCalls();
        if(bufferedCalls == 0 || bufferedCalls < this.minimumNumberOfCalls)
            return -1.0F;
        return snapshot.getFailureRate();
    }

    private float getSlowCallRate(Snapshot snapshot) {
        // 先检测样本是否足够了
        int bufferedCalls = snapshot.getTotalNumberOfCalls();
        if(bufferedCalls == 0 || bufferedCalls < this.minimumNumberOfCalls)
            return -1.0F;
        return snapshot.getSlowCallRate();
    }

    @Override
    public float getFailureRate() {
        return this.getFailureRate(this.metrics.getSnapshot());
    }

    @Override
    public float getSlowCallRate() {
        return this.getSlowCallRate(this.metrics.getSnapshot());
    }

    @Override
    public int getNumberOfSlowCalls() {
        return this.metrics.getSnapshot().getTotalNumberOfSlowCalls();
    }

    @Override
    public int getNumberOfSlowSuccessfulCalls() {
        return this.metrics.getSnapshot().getNumberOfSlowSuccessfulCalls();
    }

    @Override
    public int getNumberOfSlowFailedCalls() {
        return this.metrics.getSnapshot().getNumberOfSlowFailedCalls();
    }

    @Override
    public int getNumberOfBufferedCalls() {
        return this.metrics.getSnapshot().getTotalNumberOfCalls();
    }

    @Override
    public int getNumberOfFailedCalls() {
        return this.metrics.getSnapshot().getNumberOfFailedCalls();
    }

    @Override
    public long getNumberOfNotPermittedCalls() {
        return this.numberOfNotPermittedCalls.sum();
    }

    @Override
    public int getNumberOfSuccessfulCalls() {
        return this.metrics.getSnapshot().getNumberOfSuccessfulCalls();
    }

    enum Result {
        BELOW_THRESHOLDS,   // 失败率和慢调用率，都没有超过阈值
        FAILURE_RATE_ABOVE_THRESHOLDS,  // 只有失败率超出阈值
        SLOW_CALL_RATE_ABOVE_THRESHOLDS,    // 只有慢调用率超出阈值
        ABOVE_THRESHOLDS,   // 失败率和慢调用率，全部超出阈值
        BELOW_MINIMUM_CALLS_THRESHOLD;  // 调用次数过少（即样本过少）

        public static boolean hasExceededThresholds(Result result) {
            return hasFailureRateExceededThreshold(result) || hasSlowCallRateExceededThreshold(result);
        }

        public static boolean hasFailureRateExceededThreshold(Result result) {
            return result == ABOVE_THRESHOLDS || result == FAILURE_RATE_ABOVE_THRESHOLDS;
        }

        public static boolean hasSlowCallRateExceededThreshold(Result result) {
            return result == ABOVE_THRESHOLDS || result == SLOW_CALL_RATE_ABOVE_THRESHOLDS;
        }
    }
}
