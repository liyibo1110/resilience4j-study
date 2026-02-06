package com.github.liyibo1110.resilience4j.retry;

import com.github.liyibo1110.resilience4j.core.IntervalBiFunction;
import com.github.liyibo1110.resilience4j.core.IntervalFunction;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.core.predicate.PredicateCreator;

import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 作用等同于CircuitBreaker里面的对应Config
 * @author liyibo
 * @date 2026-02-06 10:31
 */
public class RetryConfig implements Serializable {
    private static final long serialVersionUID = 3522903275067138911L;

    public static final long DEFAULT_WAIT_DURATION = 500;
    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    /** 默认的retry间隔（IntervalFunction版本），500ms */
    private static final IntervalFunction DEFAULT_INTERVAL_FUNCTION = numOfAttempts -> DEFAULT_WAIT_DURATION;

    /** 默认的retry间隔（IntervalBiFunction版本），500ms */
    private static final IntervalBiFunction DEFAULT_INTERVAL_BI_FUNCTION = IntervalBiFunction.ofIntervalFunction(DEFAULT_INTERVAL_FUNCTION);
    private static final Predicate<Throwable> DEFAULT_RECORD_FAILURE_PREDICATE = throwable -> true;

    private Class<? extends Throwable>[] retryExceptions = new Class[0];
    private Class<? extends Throwable>[] ignoreExceptions = new Class[0];

    @Nullable
    private Predicate<Throwable> retryOnExceptionPredicate; // 需要确认这个是什么意思

    @Nullable
    private Predicate retryOnResultPredicate;   // 需要确认这个是什么意思

    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
    private boolean failAfterMaxAttempts = false;
    private boolean writableStackTraceEnabled = true;

    @Nullable
    private IntervalFunction intervalFunction;
    private IntervalBiFunction intervalBiFunction = DEFAULT_INTERVAL_BI_FUNCTION;

    /** 最终的异常Predicate */
    private Predicate<Throwable> exceptionPredicate;

    private RetryConfig() {}

    public static <T> Builder<T> custom() {
        return new Builder<>();
    }

    public static <T> Builder<T> from(RetryConfig baseConfig) {
        return new Builder<>(baseConfig);
    }

    public static RetryConfig ofDefaults() {
        return new Builder().build();
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean isFailAfterMaxAttempts() {
        return failAfterMaxAttempts;
    }

    public boolean isWritableStackTraceEnabled() {
        return writableStackTraceEnabled;
    }

    /**
     * 建议直接使用{@link RetryConfig#intervalBiFunction}
     */
    @Nullable
    @Deprecated
    public Function<Integer, Long> getIntervalFunction() {
        return intervalFunction;
    }

    public <T> IntervalBiFunction<T> getIntervalBiFunction() {
        return intervalBiFunction;
    }

    public Predicate<Throwable> getExceptionPredicate() {
        return exceptionPredicate;
    }

    @Nullable
    public <T> Predicate<T> getResultPredicate() {
        return retryOnResultPredicate;
    }

    public static class Builder<T> {
        private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
        private boolean failAfterMaxAttempts = false;
        private boolean writableStackTraceEnabled = true;

        @Nullable
        private IntervalFunction intervalFunction;
        @Nullable
        private Predicate<Throwable> retryOnExceptionPredicate;
        @Nullable
        private Predicate<T> retryOnResultPredicate;
        @Nullable
        private IntervalBiFunction<T> intervalBiFunction;

        private Class<? extends Throwable>[] retryExceptions = new Class[0];
        private Class<? extends Throwable>[] ignoreExceptions = new Class[0];

        public Builder() {}

        public Builder(RetryConfig baseConfig) {
            this.maxAttempts = baseConfig.maxAttempts;
            this.retryOnExceptionPredicate = baseConfig.retryOnExceptionPredicate;
            this.retryOnResultPredicate = baseConfig.retryOnResultPredicate;
            this.failAfterMaxAttempts = baseConfig.failAfterMaxAttempts;
            this.writableStackTraceEnabled = baseConfig.writableStackTraceEnabled;
            this.retryExceptions = baseConfig.retryExceptions;
            this.ignoreExceptions = baseConfig.ignoreExceptions;
            if(baseConfig.intervalFunction != null)
                this.intervalFunction = baseConfig.intervalFunction;
            else
                this.intervalBiFunction = baseConfig.intervalBiFunction;
        }

        public Builder<T> maxAttempts(int maxAttempts) {
            if(maxAttempts < 1)
                throw new IllegalArgumentException("maxAttempts must be greater than or equal to 1");
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder<T> waitDuration(Duration waitDuration) {
            if(waitDuration.toMillis() >= 0)
                this.intervalBiFunction = (attempt, either) -> waitDuration.toMillis();
            else
                throw new IllegalArgumentException("waitDuration must be a positive value");
            return this;
        }

        public Builder<T> retryOnResult(Predicate<T> predicate) {
            this.retryOnResultPredicate = predicate;
            return this;
        }

        public Builder<T> failAfterMaxAttempts(boolean bool) {
            this.failAfterMaxAttempts = bool;
            return this;
        }

        public Builder<T> writableStackTraceEnabled(boolean bool) {
            this.writableStackTraceEnabled = bool;
            return this;
        }

        public Builder<T> intervalFunction(IntervalFunction f) {
            this.intervalFunction = f;
            return this;
        }

        public Builder<T> intervalBiFunction(IntervalBiFunction<T> f) {
            this.intervalBiFunction = f;
            return this;
        }

        public Builder<T> retryOnException(Predicate<Throwable> predicate) {
            this.retryOnExceptionPredicate = predicate;
            return this;
        }

        @SafeVarargs
        public final Builder<T> retryExceptions(@Nullable Class<? extends Throwable>... errorClasses) {
            this.retryExceptions = errorClasses != null ? errorClasses : new Class[0];
            return this;
        }

        @SafeVarargs
        public final Builder<T> ignoreExceptions(@Nullable Class<? extends Throwable>... errorClasses) {
            this.ignoreExceptions = errorClasses != null ? errorClasses : new Class[0];
            return this;
        }

        public RetryConfig build() {
            // 只能配置1个，新版本建议使用intervalBiFunction
            if(this.intervalFunction != null && this.intervalBiFunction != null)
                throw new IllegalStateException("The intervalFunction was configured twice which could result in an" +
                        " undesired state. Please use either intervalFunction or intervalBiFunction.");
            RetryConfig config = new RetryConfig();
            config.maxAttempts = maxAttempts;
            config.failAfterMaxAttempts = failAfterMaxAttempts;
            config.writableStackTraceEnabled = writableStackTraceEnabled;
            config.retryOnExceptionPredicate = retryOnExceptionPredicate;
            config.retryOnResultPredicate = retryOnResultPredicate;
            config.retryExceptions = retryExceptions;
            config.ignoreExceptions = ignoreExceptions;
            config.exceptionPredicate = this.createExceptionPredicate();
            config.intervalFunction = this.createIntervalFunction();
            config.intervalBiFunction = Optional.ofNullable(intervalBiFunction)
                    .orElse(IntervalBiFunction.ofIntervalFunction(config.intervalFunction));
            return config;
        }

        @Nullable
        private IntervalFunction createIntervalFunction() {
            if(this.intervalFunction == null && this.intervalBiFunction == null)
                return IntervalFunction.ofDefaults();
            return this.intervalFunction;
        }

        private Predicate<Throwable> createExceptionPredicate() {
            return this.createRetryOnExceptionPredicate()
                    .and((PredicateCreator.createNegatedExceptionsPredicate(ignoreExceptions)
                            .orElse(DEFAULT_RECORD_FAILURE_PREDICATE)));
        }

        private Predicate<Throwable> createRetryOnExceptionPredicate() {
            return PredicateCreator.createExceptionsPredicate(this.retryExceptions)
                    .map(p -> this.retryOnExceptionPredicate != null ? p.or(this.retryOnExceptionPredicate) : p)
                    .orElseGet(() -> this.retryOnExceptionPredicate != null ? this.retryOnExceptionPredicate : DEFAULT_RECORD_FAILURE_PREDICATE);
        }
    }
}
