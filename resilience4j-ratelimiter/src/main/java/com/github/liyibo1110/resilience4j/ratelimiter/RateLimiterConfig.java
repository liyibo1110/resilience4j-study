package com.github.liyibo1110.resilience4j.ratelimiter;

import io.vavr.control.Either;

import java.io.Serializable;
import java.time.Duration;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * @author liyibo
 * @date 2026-02-06 17:31
 */
public class RateLimiterConfig implements Serializable {
    private static final long serialVersionUID = -1621614587284115957L;

    private static final String TIMEOUT_DURATION_MUST_NOT_BE_NULL = "TimeoutDuration must not be null";
    private static final String LIMIT_REFRESH_PERIOD_MUST_NOT_BE_NULL = "LimitRefreshPeriod must not be null";
    private static final Duration ACCEPTABLE_REFRESH_PERIOD = Duration.ofNanos(1L);
    private static final boolean DEFAULT_WRITABLE_STACK_TRACE_ENABLED = true;

    /** 核心参数：如果没有可用的许可，会等待多久 */
    private final Duration timeoutDuration;

    /** 核心参数：每批许可涉及的时间范围周期，比如1秒，意思就是1秒内允许N个许可 */
    private final Duration limitRefreshPeriod;

    /** 核心参数：每个周期放出的总许可数 */
    private final int limitForPeriod;

    private final Predicate<Either<? extends Throwable, ?>> drainPermissionsOnResult;
    private final boolean writableStackTraceEnabled;

    private RateLimiterConfig(Duration timeoutDuration, Duration limitRefreshPeriod, int limitForPeriod,
                              Predicate<Either<? extends Throwable, ?>> drainPermissionsOnResult,
                              boolean writableStackTraceEnabled) {
        this.timeoutDuration = timeoutDuration;
        this.limitRefreshPeriod = limitRefreshPeriod;
        this.limitForPeriod = limitForPeriod;
        this.drainPermissionsOnResult = drainPermissionsOnResult;
        this.writableStackTraceEnabled = writableStackTraceEnabled;
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder from(RateLimiterConfig prototype) {
        return new Builder(prototype);
    }

    public static RateLimiterConfig ofDefaults() {
        return new Builder().build();
    }

    private static Duration checkTimeoutDuration(final Duration timeoutDuration) {
        return requireNonNull(timeoutDuration, TIMEOUT_DURATION_MUST_NOT_BE_NULL);
    }

    private static Duration checkLimitRefreshPeriod(Duration limitRefreshPeriod) {
        requireNonNull(limitRefreshPeriod, LIMIT_REFRESH_PERIOD_MUST_NOT_BE_NULL);
        boolean refreshPeriodIsTooShort = limitRefreshPeriod.compareTo(ACCEPTABLE_REFRESH_PERIOD) < 0;
        if(refreshPeriodIsTooShort)
            throw new IllegalArgumentException("LimitRefreshPeriod is too short");
        return limitRefreshPeriod;
    }

    private static int checkLimitForPeriod(final int limitForPeriod) {
        if(limitForPeriod < 1)
            throw new IllegalArgumentException("LimitForPeriod should be greater than 0");
        return limitForPeriod;
    }

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public Duration getLimitRefreshPeriod() {
        return limitRefreshPeriod;
    }

    public int getLimitForPeriod() {
        return limitForPeriod;
    }

    public Predicate<Either<? extends Throwable, ?>> getDrainPermissionsOnResult() {
        return drainPermissionsOnResult;
    }

    public boolean isWritableStackTraceEnabled() {
        return writableStackTraceEnabled;
    }

    @Override
    public String toString() {
        return "RateLimiterConfig{" +
                "timeoutDuration=" + timeoutDuration +
                ", limitRefreshPeriod=" + limitRefreshPeriod +
                ", limitForPeriod=" + limitForPeriod +
                ", writableStackTraceEnabled=" + writableStackTraceEnabled +
                '}';
    }

    public static class Builder {
        private Duration timeoutDuration = Duration.ofSeconds(5);   // 5秒
        private Duration limitRefreshPeriod = Duration.ofNanos(500);    // 500纳秒
        private int limitForPeriod = 50;    // 50个并发许可
        private Predicate<Either<? extends Throwable, ?>> drainPermissionsOnResult = any -> false;  // 默认都是false
        private boolean writableStackTraceEnabled = DEFAULT_WRITABLE_STACK_TRACE_ENABLED;

        public Builder() {}

        public Builder(RateLimiterConfig prototype) {
            this.timeoutDuration = prototype.timeoutDuration;
            this.limitRefreshPeriod = prototype.limitRefreshPeriod;
            this.limitForPeriod = prototype.limitForPeriod;
            this.drainPermissionsOnResult = prototype.drainPermissionsOnResult;
            this.writableStackTraceEnabled = prototype.writableStackTraceEnabled;
        }

        public RateLimiterConfig build() {
            return new RateLimiterConfig(timeoutDuration, limitRefreshPeriod, limitForPeriod,
                    drainPermissionsOnResult, writableStackTraceEnabled);
        }

        public Builder writableStackTraceEnabled(boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        public Builder drainPermissionsOnResult(Predicate<Either<? extends Throwable, ?>> drainPermissionsOnResult) {
            this.drainPermissionsOnResult = drainPermissionsOnResult;
            return this;
        }

        public Builder timeoutDuration(final Duration timeoutDuration) {
            this.timeoutDuration = checkTimeoutDuration(timeoutDuration);
            return this;
        }

        public Builder limitRefreshPeriod(final Duration limitRefreshPeriod) {
            this.limitRefreshPeriod = checkLimitRefreshPeriod(limitRefreshPeriod);
            return this;
        }

        public Builder limitForPeriod(final int limitForPeriod) {
            this.limitForPeriod = checkLimitForPeriod(limitForPeriod);
            return this;
        }
    }
}
