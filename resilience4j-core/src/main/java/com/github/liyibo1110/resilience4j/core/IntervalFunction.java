package com.github.liyibo1110.resilience4j.core;

import io.vavr.collection.Stream;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

import static com.github.liyibo1110.resilience4j.core.IntervalFunctionCompanion.*;
/**
 * 可用于计算等待间隔的Function。
 * 输入参数是尝试次数（attempt），输出是等待间隔（毫秒），
 * 尝试次数从1开始，每次尝试后都会增加该值
 * @author liyibo
 * @date 2026-02-02 16:51
 */
@FunctionalInterface
public interface IntervalFunction extends Function<Integer, Long> {
    /** 初始化的等待间隔 */
    long DEFAULT_INITIAL_INTERVAL = 500;
    /** 默认增长系数 */
    double DEFAULT_MULTIPLIER = 1.5;
    /** 默认随机因子 */
    double DEFAULT_RANDOMIZATION_FACTOR = 0.5;

    static IntervalFunction ofDefaults() {
        return of(DEFAULT_INITIAL_INTERVAL);
    }

    /**
     * 每次返回可能变化的间隔的实例（变化由Function来控制）
     */
    static IntervalFunction of(long intervalMillis, Function<Long, Long> backoffFunction) {
        checkInterval(intervalMillis);
        Objects.requireNonNull(backoffFunction);
        return attempt -> {
            checkAttempt(attempt);
            // 非常函数式的写法，注意这里的Stream是来自varv库的，不是JDK的
            return Stream.iterate(intervalMillis, backoffFunction).get(attempt - 1);
        };
    }

    static IntervalFunction of(Duration interval, Function<Long, Long> backoffFunction) {
        return of(interval.toMillis(), backoffFunction);
    }

    /**
     * 每次返回固定间隔的实例
     */
    static IntervalFunction of(long intervalMillis) {
        checkInterval(intervalMillis);
        return attempt -> {
            checkAttempt(attempt);
            return intervalMillis;
        };
    }

    static IntervalFunction of(Duration interval) {
        return of(interval.toMillis());
    }

    /**
     * 每次返回可能变化的间隔的实例（变化由random因子来每次计算控制）
     */
    static IntervalFunction ofRandomized(long intervalMillis, double randomizationFactor) {
        checkInterval(intervalMillis);
        checkRandomizationFactor(randomizationFactor);
        return attempt -> {
            checkAttempt(attempt);
            return (long)randomize(intervalMillis, randomizationFactor);
        };
    }

    static IntervalFunction ofRandomized(Duration interval, double randomizationFactor) {
        return ofRandomized(interval.toMillis(), randomizationFactor);
    }

    static IntervalFunction ofRandomized(long intervalMillis) {
        return ofRandomized(intervalMillis, DEFAULT_RANDOMIZATION_FACTOR);
    }

    static IntervalFunction ofRandomized(Duration interval) {
        return ofRandomized(interval.toMillis(), DEFAULT_RANDOMIZATION_FACTOR);
    }

    static IntervalFunction ofRandomized() {
        return ofRandomized(DEFAULT_INITIAL_INTERVAL, DEFAULT_RANDOMIZATION_FACTOR);
    }

    /**
     * 每次返回可能变化的间隔的实例（变化增长指数和最大间隔来计算控制）
     */
    static IntervalFunction ofExponentialBackoff(long initialIntervalMillis, double multiplier, long maxIntervalMillis) {
        checkInterval(maxIntervalMillis);
        return attempt -> {
            checkAttempt(attempt);
            final long interval = ofExponentialBackoff(initialIntervalMillis, multiplier).apply(attempt);
            return Math.min(interval, maxIntervalMillis);
        };
    }

    static IntervalFunction ofExponentialBackoff(Duration initialInterval, double multiplier, Duration maxInterval) {
        return ofExponentialBackoff(initialInterval.toMillis(), multiplier, maxInterval.toMillis());
    }

    static IntervalFunction ofExponentialBackoff(long initialIntervalMillis, double multiplier) {
        checkMultiplier(multiplier);
        return of(initialIntervalMillis, x -> (long)(x * multiplier));
    }

    static IntervalFunction ofExponentialBackoff(Duration initialInterval, double multiplier) {
        return ofExponentialBackoff(initialInterval.toMillis(), multiplier);
    }

    static IntervalFunction ofExponentialBackoff(long initialIntervalMillis) {
        return ofExponentialBackoff(initialIntervalMillis, DEFAULT_MULTIPLIER);
    }

    static IntervalFunction ofExponentialBackoff(Duration initialInterval) {
        return ofExponentialBackoff(initialInterval.toMillis(), DEFAULT_MULTIPLIER);
    }

    static IntervalFunction ofExponentialBackoff() {
        return ofExponentialBackoff(DEFAULT_INITIAL_INTERVAL, DEFAULT_MULTIPLIER);
    }

    /**
     * 每次返回可能变化的间隔的实例（变化增长指数和random因子和最大间隔时间来计算控制）
     */
    static IntervalFunction ofExponentialRandomBackoff(long initialIntervalMillis, double multiplier,
                                                       double randomizationFactor, long maxIntervalMillis) {
        checkInterval(maxIntervalMillis);
        return attempt -> {
            checkAttempt(attempt);
            final long interval = ofExponentialRandomBackoff(initialIntervalMillis, multiplier, randomizationFactor).apply(attempt);
            return Math.min(interval, maxIntervalMillis);
        };
    }

    /**
     * 每次返回可能变化的间隔的实例（变化增长指数和random因子来计算控制）
     */
    static IntervalFunction ofExponentialRandomBackoff(long initialIntervalMillis, double multiplier,
                                                       double randomizationFactor) {
        checkInterval(initialIntervalMillis);
        checkMultiplier(multiplier);
        checkRandomizationFactor(randomizationFactor);
        return attempt -> {
            checkAttempt(attempt);
            final long interval = of(initialIntervalMillis, x -> (long)(x * multiplier)).apply(attempt);
            return (long)randomize(interval, randomizationFactor);
        };
    }

    static IntervalFunction ofExponentialRandomBackoff(Duration initialInterval, double multiplier,
                                                       double randomizationFactor, Duration maxInterval) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), multiplier,
                randomizationFactor, maxInterval.toMillis());
    }

    static IntervalFunction ofExponentialRandomBackoff(Duration initialInterval, double multiplier, double randomizationFactor) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), multiplier, randomizationFactor);
    }

    static IntervalFunction ofExponentialRandomBackoff(long initialIntervalMillis, double multiplier, long maxIntervalMillis) {
        return ofExponentialRandomBackoff(initialIntervalMillis, multiplier, DEFAULT_RANDOMIZATION_FACTOR, maxIntervalMillis);
    }

    static IntervalFunction ofExponentialRandomBackoff(long initialIntervalMillis, double multiplier) {
        return ofExponentialRandomBackoff(initialIntervalMillis, multiplier, DEFAULT_RANDOMIZATION_FACTOR);
    }

    static IntervalFunction ofExponentialRandomBackoff(Duration initialInterval, double multiplier, Duration maxInterval) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), multiplier, DEFAULT_RANDOMIZATION_FACTOR, maxInterval.toMillis());
    }

    static IntervalFunction ofExponentialRandomBackoff(Duration initialInterval, double multiplier) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), multiplier, DEFAULT_RANDOMIZATION_FACTOR);
    }

    static IntervalFunction ofExponentialRandomBackoff(long initialIntervalMillis) {
        return ofExponentialRandomBackoff(initialIntervalMillis, DEFAULT_MULTIPLIER);
    }

    static IntervalFunction ofExponentialRandomBackoff(Duration initialInterval) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), DEFAULT_MULTIPLIER);
    }

    static IntervalFunction ofExponentialRandomBackoff() {
        return ofExponentialRandomBackoff(DEFAULT_INITIAL_INTERVAL, DEFAULT_MULTIPLIER, DEFAULT_RANDOMIZATION_FACTOR);
    }
}

final class IntervalFunctionCompanion {
    private IntervalFunctionCompanion() {}

    /**
     * 根据current和random因子，计算出新的current值
     */
    static double randomize(final double current, final double randomizationFactor) {
        final double delta = randomizationFactor * current;
        final double min = current - delta;
        final double max = current + delta;
        return (min + (Math.random() * (max - min + 1)));
    }

    static void checkInterval(long intervalMillis) {
        if(intervalMillis < 1)
            throw new IllegalArgumentException("Illegal argument interval: " + intervalMillis + " millis is less than 1");
    }

    static void checkMultiplier(double multiplier) {
        if(multiplier < 1.0)
            throw new IllegalArgumentException("Illegal argument multiplier: " + multiplier);
    }

    static void checkRandomizationFactor(double randomizationFactor) {
        if(randomizationFactor < 0.0 || randomizationFactor >= 1.0)
            throw new IllegalArgumentException("Illegal argument randomizationFactor: " + randomizationFactor);
    }

    static void checkAttempt(long attempt) {
        if(attempt < 1)
            throw new IllegalArgumentException("Illegal argument attempt: " + attempt);
    }
}
