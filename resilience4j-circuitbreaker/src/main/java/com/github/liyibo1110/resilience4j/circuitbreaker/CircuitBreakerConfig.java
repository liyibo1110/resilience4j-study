package com.github.liyibo1110.resilience4j.circuitbreaker;

import com.github.liyibo1110.resilience4j.core.IntervalFunction;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.core.predicate.PredicateCreator;

import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author liyibo
 * @date 2026-02-05 14:02
 */
public class CircuitBreakerConfig implements Serializable {
    private static final long serialVersionUID = -5429814941777001669L;

    public static final int DEFAULT_FAILURE_RATE_THRESHOLD = 50; // Percentage
    public static final int DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100; // Percentage
    public static final int DEFAULT_WAIT_DURATION_IN_OPEN_STATE = 60; // Seconds
    public static final int DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE = 10;
    public static final int DEFAULT_MINIMUM_NUMBER_OF_CALLS = 100;
    public static final int DEFAULT_SLIDING_WINDOW_SIZE = 100;
    public static final int DEFAULT_SLOW_CALL_DURATION_THRESHOLD = 60; // Seconds
    public static final int DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE = 0; // Seconds. It is an optional parameter
    public static final SlidingWindowType DEFAULT_SLIDING_WINDOW_TYPE = SlidingWindowType.COUNT_BASED;
    public static final boolean DEFAULT_WRITABLE_STACK_TRACE_ENABLED = true;
    private static final Predicate<Throwable> DEFAULT_RECORD_EXCEPTION_PREDICATE = throwable -> true;
    private static final Predicate<Throwable> DEFAULT_IGNORE_EXCEPTION_PREDICATE = throwable -> false;
    private static final Function<Clock, Long> DEFAULT_TIMESTAMP_FUNCTION = clock -> System.nanoTime();
    private static final TimeUnit DEFAULT_TIMESTAMP_UNIT = TimeUnit.NANOSECONDS;
    private static final Predicate<Object> DEFAULT_RECORD_RESULT_PREDICATE = (Object obj) -> false;

    /** 上面都是默认值定义，下面是实际字段定义 */

    // The default exception predicate counts all exceptions as failures.
    private Predicate<Throwable> recordExceptionPredicate = DEFAULT_RECORD_EXCEPTION_PREDICATE;

    // The default exception predicate ignores no exceptions.
    private Predicate<Throwable> ignoreExceptionPredicate = DEFAULT_IGNORE_EXCEPTION_PREDICATE;
    private Function<Clock, Long> currentTimestampFunction = DEFAULT_TIMESTAMP_FUNCTION;
    private TimeUnit timestampUnit = DEFAULT_TIMESTAMP_UNIT;

    private transient Predicate<Object> recordResultPredicate = DEFAULT_RECORD_RESULT_PREDICATE;
    private Class<? extends Throwable>[] recordExceptions = new Class[0];
    private Class<? extends Throwable>[] ignoreExceptions = new Class[0];
    private float failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;
    private int permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;
    private int slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;
    private SlidingWindowType slidingWindowType = DEFAULT_SLIDING_WINDOW_TYPE;
    private int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_OF_CALLS;
    private boolean writableStackTraceEnabled = DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
    private boolean automaticTransitionFromOpenToHalfOpenEnabled = false;
    private IntervalFunction waitIntervalFunctionInOpenState = IntervalFunction.of(Duration.ofSeconds(DEFAULT_WAIT_DURATION_IN_OPEN_STATE));
    private float slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;
    private Duration slowCallDurationThreshold = Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD);
    private Duration maxWaitDurationInHalfOpenState = Duration.ofSeconds(DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE);

    private CircuitBreakerConfig() {}

    public static Builder custom() {
        return new Builder();
    }

    public static Builder from(CircuitBreakerConfig baseConfig) {
        return new Builder(baseConfig);
    }

    public static CircuitBreakerConfig ofDefaults() {
        return new Builder().build();
    }

    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    /**
     * 建议用{@link #getWaitIntervalFunctionInOpenState()}
     */
    @Deprecated
    public Duration getWaitDurationInOpenState() {
        return Duration.ofMillis(waitIntervalFunctionInOpenState.apply(1));
    }

    public IntervalFunction getWaitIntervalFunctionInOpenState() {
        return waitIntervalFunctionInOpenState;
    }

    public int getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public Predicate<Throwable> getRecordExceptionPredicate() {
        return recordExceptionPredicate;
    }

    public Predicate<Object> getRecordResultPredicate() {
        return recordResultPredicate;
    }

    public Predicate<Throwable> getIgnoreExceptionPredicate() {
        return ignoreExceptionPredicate;
    }

    public Function<Clock, Long> getCurrentTimestampFunction() {
        return currentTimestampFunction;
    }

    public TimeUnit getTimestampUnit() {
        return timestampUnit;
    }

    public boolean isAutomaticTransitionFromOpenToHalfOpenEnabled() {
        return automaticTransitionFromOpenToHalfOpenEnabled;
    }

    public int getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public boolean isWritableStackTraceEnabled() {
        return writableStackTraceEnabled;
    }

    public int getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }

    public SlidingWindowType getSlidingWindowType() {
        return slidingWindowType;
    }

    public float getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public Duration getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    public Duration getMaxWaitDurationInHalfOpenState() {
        return maxWaitDurationInHalfOpenState;
    }

    public enum SlidingWindowType {
        TIME_BASED, COUNT_BASED
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("CircuitBreakerConfig {");
        b.append("recordExceptionPredicate=");
        b.append(recordExceptionPredicate);
        b.append(", ignoreExceptionPredicate=");
        b.append(ignoreExceptionPredicate);
        b.append(", recordExceptions=");
        b.append(Arrays.toString(recordExceptions));
        b.append(", ignoreExceptions=");
        b.append(Arrays.toString(ignoreExceptions));
        b.append(", failureRateThreshold=");
        b.append(failureRateThreshold);
        b.append(", permittedNumberOfCallsInHalfOpenState=");
        b.append(permittedNumberOfCallsInHalfOpenState);
        b.append(", slidingWindowSize=");
        b.append(slidingWindowSize);
        b.append(", slidingWindowType=");
        b.append(slidingWindowType);
        b.append(", minimumNumberOfCalls=");
        b.append(minimumNumberOfCalls);
        b.append(", writableStackTraceEnabled=");
        b.append(writableStackTraceEnabled);
        b.append(", automaticTransitionFromOpenToHalfOpenEnabled=");
        b.append(automaticTransitionFromOpenToHalfOpenEnabled);
        b.append(", waitIntervalFunctionInOpenState=");
        b.append(waitIntervalFunctionInOpenState);
        b.append(", slowCallRateThreshold=");
        b.append(slowCallRateThreshold);
        b.append(", slowCallDurationThreshold=");
        b.append(slowCallDurationThreshold);
        b.append("}");
        return b.toString();
    }

    public static class Builder {
        @Nullable
        private Predicate<Throwable> recordExceptionPredicate;
        @Nullable
        private Predicate<Throwable> ignoreExceptionPredicate;
        private Function<Clock, Long> currentTimestampFunction = DEFAULT_TIMESTAMP_FUNCTION;
        private TimeUnit timestampUnit = DEFAULT_TIMESTAMP_UNIT;


        private Class<? extends Throwable>[] recordExceptions = new Class[0];
        private Class<? extends Throwable>[] ignoreExceptions = new Class[0];

        private float failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;
        private int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_OF_CALLS;
        private boolean writableStackTraceEnabled = DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
        private int permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;
        private int slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;
        private Predicate<Object> recordResultPredicate = DEFAULT_RECORD_RESULT_PREDICATE;

        private IntervalFunction waitIntervalFunctionInOpenState = IntervalFunction.of(Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD));

        private boolean automaticTransitionFromOpenToHalfOpenEnabled = false;
        private SlidingWindowType slidingWindowType = DEFAULT_SLIDING_WINDOW_TYPE;
        private float slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;
        private Duration slowCallDurationThreshold = Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD);
        private Duration maxWaitDurationInHalfOpenState = Duration.ofSeconds(DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE);
        private byte createWaitIntervalFunctionCounter = 0;

        public Builder(CircuitBreakerConfig baseConfig) {
            this.waitIntervalFunctionInOpenState = baseConfig.waitIntervalFunctionInOpenState;
            this.permittedNumberOfCallsInHalfOpenState = baseConfig.permittedNumberOfCallsInHalfOpenState;
            this.slidingWindowSize = baseConfig.slidingWindowSize;
            this.slidingWindowType = baseConfig.slidingWindowType;
            this.minimumNumberOfCalls = baseConfig.minimumNumberOfCalls;
            this.failureRateThreshold = baseConfig.failureRateThreshold;
            this.ignoreExceptions = baseConfig.ignoreExceptions;
            this.recordExceptions = baseConfig.recordExceptions;
            this.recordExceptionPredicate = baseConfig.recordExceptionPredicate;
            this.ignoreExceptionPredicate = baseConfig.ignoreExceptionPredicate;
            this.currentTimestampFunction = baseConfig.currentTimestampFunction;
            this.timestampUnit = baseConfig.timestampUnit;
            this.automaticTransitionFromOpenToHalfOpenEnabled = baseConfig.automaticTransitionFromOpenToHalfOpenEnabled;
            this.slowCallRateThreshold = baseConfig.slowCallRateThreshold;
            this.slowCallDurationThreshold = baseConfig.slowCallDurationThreshold;
            this.maxWaitDurationInHalfOpenState = baseConfig.maxWaitDurationInHalfOpenState;
            this.writableStackTraceEnabled = baseConfig.writableStackTraceEnabled;
            this.recordResultPredicate = baseConfig.recordResultPredicate;
        }

        public Builder() {}

        public Builder failureRateThreshold(float failureRateThreshold) {
            if(failureRateThreshold <= 0 || failureRateThreshold > 100)
                throw new IllegalArgumentException("failureRateThreshold must be between 1 and 100");
            this.failureRateThreshold = failureRateThreshold;
            return this;
        }

        public Builder slowCallRateThreshold(float slowCallRateThreshold) {
            if(slowCallRateThreshold <= 0 || slowCallRateThreshold > 100)
                throw new IllegalArgumentException("slowCallRateThreshold must be between 1 and 100");
            this.slowCallRateThreshold = slowCallRateThreshold;
            return this;
        }

        public Builder writableStackTraceEnabled(boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        public Builder waitDurationInOpenState(Duration waitDurationInOpenState) {
            long waitDurationInMillis = waitDurationInOpenState.toMillis();
            if(waitDurationInMillis < 1)
                throw new IllegalArgumentException("waitDurationInOpenState must be at least 1[ms]");
            this.waitIntervalFunctionInOpenState = IntervalFunction.of(waitDurationInMillis);
            createWaitIntervalFunctionCounter++;
            return this;
        }

        public Builder waitIntervalFunctionInOpenState(IntervalFunction waitIntervalFunctionInOpenState) {
            this.waitIntervalFunctionInOpenState = waitIntervalFunctionInOpenState;
            createWaitIntervalFunctionCounter++;
            return this;
        }

        public Builder slowCallDurationThreshold(Duration slowCallDurationThreshold) {
            if(slowCallDurationThreshold.toNanos() < 1)
                throw new IllegalArgumentException("slowCallDurationThreshold must be at least 1[ns]");
            this.slowCallDurationThreshold = slowCallDurationThreshold;
            return this;
        }

        public Builder maxWaitDurationInHalfOpenState(Duration maxWaitDurationInHalfOpenState) {
            if(maxWaitDurationInHalfOpenState.toMillis() < 1)
                throw new IllegalArgumentException("maxWaitDurationInHalfOpenState must be at least 1[ms]");
            this.maxWaitDurationInHalfOpenState = maxWaitDurationInHalfOpenState;
            return this;
        }

        public Builder permittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            if(permittedNumberOfCallsInHalfOpenState < 1)
                throw new IllegalArgumentException("permittedNumberOfCallsInHalfOpenState must be greater than 0");
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            return this;
        }

        /**
         * 建议使用{@link #permittedNumberOfCallsInHalfOpenState(int)}
         */
        @Deprecated
        public Builder ringBufferSizeInHalfOpenState(int ringBufferSizeInHalfOpenState) {
            if(ringBufferSizeInHalfOpenState < 1)
                throw new IllegalArgumentException("ringBufferSizeInHalfOpenState must be greater than 0");
            this.permittedNumberOfCallsInHalfOpenState = ringBufferSizeInHalfOpenState;
            return this;
        }

        /**
         * 建议使用{@link #slidingWindow(int, int, SlidingWindowType)}
         */
        @Deprecated
        public Builder ringBufferSizeInClosedState(int ringBufferSizeInClosedState) {
            if(ringBufferSizeInClosedState < 1)
                throw new IllegalArgumentException("ringBufferSizeInClosedState must be greater than 0");
            return slidingWindow(ringBufferSizeInClosedState, ringBufferSizeInClosedState,
                    SlidingWindowType.COUNT_BASED);
        }

        public Builder slidingWindow(int slidingWindowSize, int minimumNumberOfCalls, SlidingWindowType slidingWindowType) {
            if(slidingWindowSize < 1)
                throw new IllegalArgumentException("slidingWindowSize must be greater than 0");
            if(minimumNumberOfCalls < 1)
                throw new IllegalArgumentException("minimumNumberOfCalls must be greater than 0");

            if(slidingWindowType == SlidingWindowType.COUNT_BASED)
                this.minimumNumberOfCalls = Math.min(minimumNumberOfCalls, slidingWindowSize);
            else
                this.minimumNumberOfCalls = minimumNumberOfCalls;

            this.slidingWindowSize = slidingWindowSize;
            this.slidingWindowType = slidingWindowType;
            return this;
        }

        public Builder slidingWindowSize(int slidingWindowSize) {
            if(slidingWindowSize < 1)
                throw new IllegalArgumentException("slidingWindowSize must be greater than 0");
            this.slidingWindowSize = slidingWindowSize;
            return this;
        }

        public Builder minimumNumberOfCalls(int minimumNumberOfCalls) {
            if(minimumNumberOfCalls < 1)
                throw new IllegalArgumentException("minimumNumberOfCalls must be greater than 0");
            this.minimumNumberOfCalls = minimumNumberOfCalls;
            return this;
        }

        public Builder slidingWindowType(SlidingWindowType slidingWindowType) {
            this.slidingWindowType = slidingWindowType;
            return this;
        }

        /**
         * 建议使用{@link #recordException(Predicate)}
         */
        @Deprecated
        public Builder recordFailure(Predicate<Throwable> predicate) {
            this.recordExceptionPredicate = predicate;
            return this;
        }

        public Builder recordException(Predicate<Throwable> predicate) {
            this.recordExceptionPredicate = predicate;
            return this;
        }

        public Builder currentTimestampFunction(Function<Clock, Long> currentTimestampFunction, TimeUnit timeUnit) {
            this.timestampUnit = timeUnit;
            this.currentTimestampFunction = currentTimestampFunction;
            return this;
        }

        public Builder recordResult(Predicate<Object> predicate) {
            this.recordResultPredicate = predicate;
            return this;
        }

        public Builder ignoreException(Predicate<Throwable> predicate) {
            this.ignoreExceptionPredicate = predicate;
            return this;
        }

        @SafeVarargs
        public final Builder recordExceptions(@Nullable Class<? extends Throwable>... errorClasses) {
            this.recordExceptions = errorClasses != null ? errorClasses : new Class[0];
            return this;
        }

        @SafeVarargs
        public final Builder ignoreExceptions(@Nullable Class<? extends Throwable>... errorClasses) {
            this.ignoreExceptions = errorClasses != null ? errorClasses : new Class[0];
            return this;
        }

        public Builder enableAutomaticTransitionFromOpenToHalfOpen() {
            this.automaticTransitionFromOpenToHalfOpenEnabled = true;
            return this;
        }

        public Builder automaticTransitionFromOpenToHalfOpenEnabled(
                boolean enableAutomaticTransitionFromOpenToHalfOpen) {
            this.automaticTransitionFromOpenToHalfOpenEnabled = enableAutomaticTransitionFromOpenToHalfOpen;
            return this;
        }

        public CircuitBreakerConfig build() {
            CircuitBreakerConfig config = new CircuitBreakerConfig();
            config.waitIntervalFunctionInOpenState = this.validateWaitIntervalFunctionInOpenState();
            config.slidingWindowType = slidingWindowType;
            config.slowCallDurationThreshold = slowCallDurationThreshold;
            config.maxWaitDurationInHalfOpenState = maxWaitDurationInHalfOpenState;
            config.slowCallRateThreshold = slowCallRateThreshold;
            config.failureRateThreshold = failureRateThreshold;
            config.slidingWindowSize = slidingWindowSize;
            config.minimumNumberOfCalls = minimumNumberOfCalls;
            config.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            config.recordExceptions = recordExceptions;
            config.ignoreExceptions = ignoreExceptions;
            config.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
            config.writableStackTraceEnabled = writableStackTraceEnabled;
            config.recordExceptionPredicate = this.createRecordExceptionPredicate();
            config.ignoreExceptionPredicate = this.createIgnoreFailurePredicate();
            config.currentTimestampFunction = currentTimestampFunction;
            config.timestampUnit = timestampUnit;
            config.recordResultPredicate = recordResultPredicate;
            return config;
        }

        private Predicate<Throwable> createRecordExceptionPredicate() {
            return PredicateCreator.createExceptionsPredicate(this.recordExceptions)
                    .map(p -> this.recordExceptionPredicate != null ? p.or(this.recordExceptionPredicate) : p)
                    .orElseGet(() -> this.recordExceptionPredicate != null ? this.recordExceptionPredicate : DEFAULT_RECORD_EXCEPTION_PREDICATE);
        }

        private Predicate<Throwable> createIgnoreFailurePredicate() {
            return PredicateCreator.createExceptionsPredicate(this.ignoreExceptions)
                    .map(p -> this.ignoreExceptionPredicate != null ? p.or(this.ignoreExceptionPredicate) : p)
                    .orElseGet(() -> this.ignoreExceptionPredicate != null ? this.ignoreExceptionPredicate : DEFAULT_IGNORE_EXCEPTION_PREDICATE);
        }

        private IntervalFunction validateWaitIntervalFunctionInOpenState() {
            if(createWaitIntervalFunctionCounter > 1)
                throw new IllegalStateException("The waitIntervalFunction was configured multiple times " +
                        "which could result in an undesired state. Please verify that waitIntervalFunctionInOpenState " +
                        "and waitDurationInOpenState are not used together.");
            return this.waitIntervalFunctionInOpenState;
        }
    }
}
