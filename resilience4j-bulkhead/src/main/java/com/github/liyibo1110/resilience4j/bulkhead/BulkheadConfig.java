package com.github.liyibo1110.resilience4j.bulkhead;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.time.Duration;

/**
 * SemaphoreBulkhead实现专用的Config
 * @author liyibo
 * @date 2026-02-08 20:24
 */
@Immutable
public class BulkheadConfig implements Serializable {

    private static final long serialVersionUID = -9139631465007403460L;

    /** 默认最大并发数 */
    public static final int DEFAULT_MAX_CONCURRENT_CALLS = 25;
    public static final Duration DEFAULT_MAX_WAIT_DURATION = Duration.ofSeconds(0);
    public static final boolean DEFAULT_WRITABLE_STACK_TRACE_ENABLED = true;
    public static final boolean DEFAULT_FAIR_CALL_HANDLING_STRATEGY_ENABLED = true;

    /** 最大并发数 */
    private final int maxConcurrentCalls;

    /** 等待时间 */
    private final Duration maxWaitDuration;

    private final boolean writableStackTraceEnabled;
    private final boolean fairCallHandlingEnabled;

    private BulkheadConfig(int maxConcurrentCalls, Duration maxWaitDuration,
                           boolean writableStackTraceEnabled, boolean fairCallHandlingEnabled) {
        this.maxConcurrentCalls = maxConcurrentCalls;
        this.maxWaitDuration = maxWaitDuration;
        this.writableStackTraceEnabled = writableStackTraceEnabled;
        this.fairCallHandlingEnabled = fairCallHandlingEnabled;
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder from(BulkheadConfig baseConfig) {
        return new Builder(baseConfig);
    }

    public static BulkheadConfig ofDefaults() {
        return new Builder().build();
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public Duration getMaxWaitDuration() {
        return maxWaitDuration;
    }

    public boolean isWritableStackTraceEnabled() {
        return writableStackTraceEnabled;
    }

    public boolean isFairCallHandlingEnabled() {
        return fairCallHandlingEnabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BulkheadConfig{");
        sb.append("maxConcurrentCalls=").append(maxConcurrentCalls);
        sb.append(", maxWaitDuration=").append(maxWaitDuration);
        sb.append(", writableStackTraceEnabled=").append(writableStackTraceEnabled);
        sb.append(", fairCallHandlingEnabled=").append(fairCallHandlingEnabled);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private int maxConcurrentCalls;
        private Duration maxWaitDuration;
        private boolean writableStackTraceEnabled;
        private boolean fairCallHandlingEnabled;

        public Builder() {
            this.maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;
            this.maxWaitDuration = DEFAULT_MAX_WAIT_DURATION;
            this.writableStackTraceEnabled = DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
            this.fairCallHandlingEnabled = DEFAULT_FAIR_CALL_HANDLING_STRATEGY_ENABLED;
        }

        public Builder(BulkheadConfig bulkheadConfig) {
            this.maxConcurrentCalls = bulkheadConfig.getMaxConcurrentCalls();
            this.maxWaitDuration = bulkheadConfig.getMaxWaitDuration();
            this.writableStackTraceEnabled = bulkheadConfig.isWritableStackTraceEnabled();
            this.fairCallHandlingEnabled = bulkheadConfig.isFairCallHandlingEnabled();
        }

        public Builder maxConcurrentCalls(int maxConcurrentCalls) {
            if(maxConcurrentCalls < 0)
                throw new IllegalArgumentException("maxConcurrentCalls must be an integer value >= 0");
            this.maxConcurrentCalls = maxConcurrentCalls;
            return this;
        }

        public Builder maxWaitDuration(Duration maxWaitDuration) {
            if(maxWaitDuration.toMillis() < 0)
                throw new IllegalArgumentException("maxWaitDuration must be a positive integer value >= 0");
            this.maxWaitDuration = maxWaitDuration;
            return this;
        }

        public Builder writableStackTraceEnabled(boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        public Builder fairCallHandlingStrategyEnabled(boolean fairCallHandlingEnabled) {
            this.fairCallHandlingEnabled = fairCallHandlingEnabled;
            return this;
        }

        public BulkheadConfig build() {
            return new BulkheadConfig(maxConcurrentCalls, maxWaitDuration, writableStackTraceEnabled, fairCallHandlingEnabled);
        }
    }
}
