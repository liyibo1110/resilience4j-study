package com.github.liyibo1110.resilience4j.timelimiter;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

/**
 * @author liyibo
 * @date 2026-02-08 18:23
 */
public class TimeLimiterConfig implements Serializable {
    private static final long serialVersionUID = 2203981592465761602L;

    private static final String TIMEOUT_DURATION_MUST_NOT_BE_NULL = "TimeoutDuration must not be null";

    /** 默认异步任务只能执行1秒 */
    private Duration timeoutDuration = Duration.ofSeconds(1);

    /** 超时了是否要cancel原来的Future实例 */
    private boolean cancelRunningFuture = true;

    private TimeLimiterConfig() {}

    public static Builder custom() {
        return new Builder();
    }

    public static Builder from(TimeLimiterConfig baseConfig) {
        return new Builder(baseConfig);
    }

    public static TimeLimiterConfig ofDefaults() {
        return new Builder().build();
    }

    private static Duration checkTimeoutDuration(final Duration timeoutDuration) {
        return Objects.requireNonNull(timeoutDuration, TIMEOUT_DURATION_MUST_NOT_BE_NULL);
    }

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public boolean shouldCancelRunningFuture() {
        return cancelRunningFuture;
    }

    @Override
    public String toString() {
        return "TimeLimiterConfig{" +
                "timeoutDuration=" + timeoutDuration +
                "cancelRunningFuture=" + cancelRunningFuture +
                '}';
    }

    public static class Builder {
        private Duration timeoutDuration = Duration.ofSeconds(1);
        private boolean cancelRunningFuture = true;

        public Builder() {}

        public Builder(TimeLimiterConfig baseConfig) {
            this.timeoutDuration = baseConfig.timeoutDuration;
            this.cancelRunningFuture = baseConfig.cancelRunningFuture;
        }

        public TimeLimiterConfig build() {
            TimeLimiterConfig config = new TimeLimiterConfig();
            config.timeoutDuration = timeoutDuration;
            config.cancelRunningFuture = cancelRunningFuture;
            return config;
        }

        public Builder timeoutDuration(final Duration timeoutDuration) {
            this.timeoutDuration = checkTimeoutDuration(timeoutDuration);
            return this;
        }

        public Builder cancelRunningFuture(final boolean cancelRunningFuture) {
            this.cancelRunningFuture = cancelRunningFuture;
            return this;
        }
    }
}
