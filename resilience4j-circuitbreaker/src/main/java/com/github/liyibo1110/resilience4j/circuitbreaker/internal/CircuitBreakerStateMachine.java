package com.github.liyibo1110.resilience4j.circuitbreaker.internal;

import com.github.liyibo1110.resilience4j.circuitbreaker.CallNotPermittedException;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.circuitbreaker.ResultRecordedAsFailureException;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnCallNotPermittedEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnFailureRateExceededEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnIgnoredErrorEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnResetEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnSlowCallRateExceededEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnSuccessEvent;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.EventProcessor;
import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * CircuitBreaker的实现类，负责状态的切换
 * @author liyibo
 * @date 2026-02-05 14:39
 */
public final class CircuitBreakerStateMachine implements CircuitBreaker {
    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreakerStateMachine.class);

    private final String name;
    private final AtomicReference<CircuitBreakerState> stateReference;
    private final CircuitBreakerConfig circuitBreakerConfig;
    private final Map<String, String> tags;
    private final CircuitBreakerEventProcessor eventProcessor;
    private final Clock clock;
    private final SchedulerFactory schedulerFactory;
    private final Function<Clock, Long> currentTimestampFunction;
    private final TimeUnit timestampUnit;

    public CircuitBreakerStateMachine(String name) {
        this(name, CircuitBreakerConfig.ofDefaults());
    }

    public CircuitBreakerStateMachine(String name, Supplier<CircuitBreakerConfig> configSupplier) {
        this(name, configSupplier.get());
    }

    public CircuitBreakerStateMachine(String name, Supplier<CircuitBreakerConfig> configSupplier,
                                      io.vavr.collection.Map<String, String> tags) {
        this(name, configSupplier.get(), tags);
    }

    public CircuitBreakerStateMachine(String name, CircuitBreakerConfig config) {
        this(name, config, Clock.systemUTC());
    }

    public CircuitBreakerStateMachine(String name, CircuitBreakerConfig config,
                                      io.vavr.collection.Map<String, String> tags) {
        this(name, config, Clock.systemUTC(), tags);
    }

    public CircuitBreakerStateMachine(String name, CircuitBreakerConfig config,
                                      SchedulerFactory schedulerFactory) {
        this(name, config, Clock.systemUTC(), schedulerFactory, HashMap.empty());
    }

    public CircuitBreakerStateMachine(String name, CircuitBreakerConfig config,
                                      Clock clock) {
        this(name, config, clock, SchedulerFactory.getInstance(), HashMap.empty());
    }

    public CircuitBreakerStateMachine(String name, CircuitBreakerConfig config,
                                      Clock clock, io.vavr.collection.Map<String, String> tags) {
        this(name, config, clock, SchedulerFactory.getInstance(), tags);
    }

    private CircuitBreakerStateMachine(String name, CircuitBreakerConfig config,
                                       Clock clock, SchedulerFactory schedulerFactory,
                                       io.vavr.collection.Map<String, String> tags) {
        this.name = name;
        this.circuitBreakerConfig = Objects.requireNonNull(config, "Config must not be null");
        this.eventProcessor = new CircuitBreakerEventProcessor();
        this.clock = clock;
        this.stateReference = new AtomicReference<>(new ClosedState()); // 默认是CLOSED状态
        this.schedulerFactory = schedulerFactory;
        this.tags = Objects.requireNonNull(tags, "Tags must not be null");
        this.currentTimestampFunction = config.getCurrentTimestampFunction();
        this.timestampUnit = config.getTimestampUnit();
    }

    @Override
    public long getCurrentTimestamp() {
        return this.currentTimestampFunction.apply(clock);
    }

    @Override
    public TimeUnit getTimestampUnit() {
        return this.timestampUnit;
    }

    @Override
    public boolean tryAcquirePermission() {
        // 委托调用State实例的tryAcquirePermission
        boolean callPermitted = stateReference.get().tryAcquirePermission();
        if(!callPermitted)
            this.publishCallNotPermittedEvent();    // 发event
        return callPermitted;
    }

    @Override
    public void releasePermission() {
        this.stateReference.get().releasePermission();
    }

    @Override
    public void acquirePermission() {
        try {
            this.stateReference.get().acquirePermission();
        } catch (Exception e) {
            this.publishCallNotPermittedEvent();    // 发event
            throw e;
        }
    }

    @Override
    public void onError(long duration, TimeUnit unit, Throwable t) {
        if(t instanceof CompletionException || t instanceof ExecutionException) {
            Throwable cause = t.getCause();
            this.handleThrowable(duration, unit, cause);
        }else {
            this.handleThrowable(duration, unit, t);
        }
    }

    private void handleThrowable(long duration, TimeUnit unit, Throwable t) {
        if(this.circuitBreakerConfig.getIgnoreExceptionPredicate().test(t)) {   // 是否在ignore名单
            LOG.debug("CircuitBreaker '{}' ignored an exception:", name, t);
            this.releasePermission();
            this.publishCircuitIgnoredErrorEvent(name, duration, unit, t);
        }else if(this.circuitBreakerConfig.getRecordExceptionPredicate().test(t)) { // 是否在record名单
            LOG.debug("CircuitBreaker '{}' recorded an exception as failure:", name, t);
            this.publishCircuitErrorEvent(name, duration, unit, t);
            this.stateReference.get().onError(duration, unit, t);
        }else {
            LOG.debug("CircuitBreaker '{}' recorded an exception as success:", name, t);
            this.publishSuccessEvent(duration, unit);
            this.stateReference.get().onSuccess(duration, unit);
        }
    }

    @Override
    public void onSuccess(long duration, TimeUnit unit) {
        LOG.debug("CircuitBreaker '{}' succeeded:", name);
        this.publishSuccessEvent(duration, unit);
        this.stateReference.get().onSuccess(duration, unit);
    }

    @Override
    public void onResult(long duration, TimeUnit unit, @Nullable Object result) {
        if(result != null && this.circuitBreakerConfig.getRecordResultPredicate().test(result)) {
            LOG.debug("CircuitBreaker '{}' recorded a result type '{}' as failure:", name, result.getClass());
            ResultRecordedAsFailureException failure = new ResultRecordedAsFailureException(name, result);
            this.publishCircuitErrorEvent(name, duration, unit, failure);
            this.stateReference.get().onError(duration, unit, failure);
        }else {
            this.onSuccess(duration, unit);
        }
    }

    @Override
    public State getState() {
        return this.stateReference.get().getState();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CircuitBreakerConfig getCircuitBreakerConfig() {
        return this.circuitBreakerConfig;
    }

    @Override
    public Metrics getMetrics() {
        return this.stateReference.get().getMetrics();
    }

    @Override
    public Map<String, String> getTags() {
        return this.tags;
    }

    @Override
    public String toString() {
        return String.format("CircuitBreaker '%s'", this.name);
    }

    @Override
    public void reset() {
        CircuitBreakerState previousState = stateReference.getAndUpdate(currentState -> new ClosedState());
        if(previousState.getState() != State.CLOSED)
            this.publishStateTransitionEvent(StateTransition.transitionBetween(this.getName(), previousState.getState(), State.CLOSED));
        this.publishResetEvent();
    }

    /**
     * 切换至新状态
     */
    private void stateTransition(State newState, UnaryOperator<CircuitBreakerState> newStateGenerator) {
        CircuitBreakerState previousState = this.stateReference.getAndUpdate(currentState -> {
            // 没有用到返回值，就是检测一下newState的值是否正常
            StateTransition.transitionBetween(getName(), currentState.getState(), newState);
            currentState.preTransitionHook();   // 切换之前调用一下钩子方法
            return newStateGenerator.apply(currentState);
        });
        this.publishStateTransitionEvent(StateTransition.transitionBetween(this.getName(), previousState.getState(), newState));
    }

    @Override
    public void transitionToDisabledState() {
        this.stateTransition(State.DISABLED, currentState -> new DisabledState());
    }

    @Override
    public void transitionToMetricsOnlyState() {
        this.stateTransition(State.METRICS_ONLY, currentState -> new MetricsOnlyState());
    }

    @Override
    public void transitionToForcedOpenState() {
        this.stateTransition(State.FORCED_OPEN, currentState -> new ForcedOpenState(currentState.attempts() + 1));
    }

    @Override
    public void transitionToClosedState() {
        this.stateTransition(State.CLOSED, currentState -> new ClosedState());
    }

    @Override
    public void transitionToOpenState() {
        this.stateTransition(State.OPEN, currentState -> new OpenState(currentState.attempts() + 1, currentState.getMetrics()));
    }

    @Override
    public void transitionToHalfOpenState() {
        this.stateTransition(State.HALF_OPEN, currentState -> new HalfOpenState(currentState.attempts()));
    }

    /**
     * 判断event是否应该发出（就是检查event实例自身内部的forcePublish开关，以及CircuitBreaker.State的开关，有1个开就要发）
     */
    private boolean shouldPublishEvents(CircuitBreakerEvent event) {
        return this.stateReference.get().shouldPublishEvents(event);
    }

    /**
     * 尝试发布event
     */
    private void publishEventIfHasConsumer(CircuitBreakerEvent event) {
        if(!eventProcessor.hasConsumers()) {
            LOG.debug("No Consumers: Event {} not published", event.getEventType());
            return;
        }
        this.publishEvent(event);
    }

    /**
     * 发布event
     */
    private void publishEvent(CircuitBreakerEvent event) {
        if(this.shouldPublishEvents(event)) {
            try {
                this.eventProcessor.consumeEvent(event);
                LOG.debug("Event {} published: {}", event.getEventType(), event);
            } catch (Throwable t) {
                LOG.warn("Failed to handle event {}", event.getEventType(), t);
            }
        }else {
            LOG.debug("Publishing not allowed: Event {} not published", event.getEventType());
        }
    }

    private void publishStateTransitionEvent(final StateTransition stateTransition) {
        if(StateTransition.isInternalTransition(stateTransition))   // 状态要变化了才可以
            return;
        this.publishEventIfHasConsumer(new CircuitBreakerOnStateTransitionEvent(name, stateTransition));
    }

    private void publishResetEvent() {
        this.publishEventIfHasConsumer(new CircuitBreakerOnResetEvent(this.name));
    }

    private void publishCallNotPermittedEvent() {
        this.publishEventIfHasConsumer(new CircuitBreakerOnCallNotPermittedEvent(this.name));
    }

    private void publishSuccessEvent(final long duration, TimeUnit unit) {
        if(this.eventProcessor.hasConsumers())
            this.publishEvent(new CircuitBreakerOnSuccessEvent(this.name, this.elapsedDuration(duration, unit)));
    }

    private Duration elapsedDuration(final long duration, TimeUnit unit) {
        return Duration.ofNanos(unit.toNanos(duration));
    }

    private void publishCircuitErrorEvent(final String name, final long duration, final TimeUnit unit, final Throwable t) {
        if(this.eventProcessor.hasConsumers()) {
            final Duration elapsedDuration = this.elapsedDuration(duration, unit);
            this.publishEvent(new CircuitBreakerOnErrorEvent(name, elapsedDuration, t));
        }
    }

    private void publishCircuitIgnoredErrorEvent(final String name, long duration, final TimeUnit unit, final Throwable t) {
        final Duration elapsedDuration = this.elapsedDuration(duration, unit);
        this.publishEventIfHasConsumer(new CircuitBreakerOnIgnoredErrorEvent(name, elapsedDuration, t));
    }

    private void publishCircuitFailureRateExceededEvent(final String name, float failureRate) {
        this.publishEventIfHasConsumer(new CircuitBreakerOnFailureRateExceededEvent(name, failureRate));
    }

    private void publishCircuitSlowCallRateExceededEvent(final String name, float slowCallRate) {
        this.publishEventIfHasConsumer(new CircuitBreakerOnSlowCallRateExceededEvent(name, slowCallRate));
    }

    private void publishCircuitThresholdsExceededEvent(final CircuitBreakerMetrics.Result result,
                                                       final CircuitBreakerMetrics metrics) {
        if(CircuitBreakerMetrics.Result.hasFailureRateExceededThreshold(result))
            this.publishCircuitFailureRateExceededEvent(this.getName(), metrics.getFailureRate());
        if(CircuitBreakerMetrics.Result.hasSlowCallRateExceededThreshold(result))
            this.publishCircuitSlowCallRateExceededEvent(this.getName(), metrics.getSlowCallRate());
    }

    @Override
    public EventPublisher getEventPublisher() {
        return eventProcessor;
    }

    private interface CircuitBreakerState {

        boolean tryAcquirePermission();

        void acquirePermission();

        void releasePermission();

        void onSuccess(long duration, TimeUnit unit);

        void onError(long duration, TimeUnit unit, Throwable t);

        int attempts();

        CircuitBreaker.State getState();

        CircuitBreakerMetrics getMetrics();

        /**
         * 判断cb在当前状态中，是否应该发event
         */
        default boolean shouldPublishEvents(CircuitBreakerEvent event) {
            return event.getEventType().forcePublish || this.getState().allowPublish;
        }

        /**
         * 在状态转换之前的钩子方法，可以做一些事情
         */
        default void preTransitionHook() {
            // nothing to do
        }
    }

    /**
     * 还是三合一
     */
    private class CircuitBreakerEventProcessor extends EventProcessor<CircuitBreakerEvent>
                implements EventConsumer<CircuitBreakerEvent>, EventPublisher {

        @Override
        public EventPublisher onSuccess(EventConsumer<CircuitBreakerOnSuccessEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnSuccessEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onError(EventConsumer<CircuitBreakerOnErrorEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnErrorEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onStateTransition(EventConsumer<CircuitBreakerOnStateTransitionEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnStateTransitionEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onReset(EventConsumer<CircuitBreakerOnResetEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnResetEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onIgnoredError(EventConsumer<CircuitBreakerOnIgnoredErrorEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnIgnoredErrorEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onCallNotPermitted(EventConsumer<CircuitBreakerOnCallNotPermittedEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnCallNotPermittedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onFailureRateExceeded(EventConsumer<CircuitBreakerOnFailureRateExceededEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnFailureRateExceededEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onSlowCallRateExceeded(EventConsumer<CircuitBreakerOnSlowCallRateExceededEvent> eventConsumer) {
            this.registerConsumer(CircuitBreakerOnSlowCallRateExceededEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public void consumeEvent(CircuitBreakerEvent event) {
            super.processEvent(event);
        }
    }

    private class ClosedState implements CircuitBreakerState {
        private final CircuitBreakerMetrics metrics;

        /** 状态是否为关闭 */
        private final AtomicBoolean isClosed;

        ClosedState() {
            this.metrics = CircuitBreakerMetrics.forClosed(getCircuitBreakerConfig(), clock);
            this.isClosed = new AtomicBoolean(true);
        }

        @Override
        public boolean tryAcquirePermission() {
            return this.isClosed.get();
        }

        @Override
        public void acquirePermission() {
            // nothing to do
        }

        @Override
        public void releasePermission() {
            // nothing to do
        }

        @Override
        public void onSuccess(long duration, TimeUnit unit) {
            // 委托給metrics去记录，并返回阈值判断结果，后面重复的方法不写了
            this.checkIfThresholdsExceeded(metrics.onSuccess(duration, unit));
        }

        @Override
        public void onError(long duration, TimeUnit unit, Throwable t) {
            this.checkIfThresholdsExceeded(metrics.onError(duration, unit));
        }

        @Override
        public int attempts() {
            return 0;
        }

        @Override
        public State getState() {
            return CircuitBreaker.State.CLOSED;
        }

        @Override
        public CircuitBreakerMetrics getMetrics() {
            return this.metrics;
        }

        /**
         * 根据metrics的统计结果返回的Result，判断是否要切换状态
         */
        private void checkIfThresholdsExceeded(CircuitBreakerMetrics.Result result) {
            if(CircuitBreakerMetrics.Result.hasExceededThresholds(result)) {
                if(this.isClosed.compareAndSet(true, false)) {
                    // 发布超出断路阈值event
                    publishCircuitThresholdsExceededEvent(result, this.metrics);
                    // 切换到OPEN状态
                    transitionToOpenState();
                }
            }
        }
    }

    private class OpenState implements CircuitBreakerState {
        /** 当前第几个熔断周期，不是spring retry里面的调用次数 */
        private final int attempts;

        /** 下次尝试半开运行的时间点 */
        private final Instant retryAfterWaitDuration;
        private final CircuitBreakerMetrics metrics;

        /** 状态是否为打开 */
        private final AtomicBoolean isOpen;

        /** 切换到半开状态的延迟线程调用结果 */
        @Nullable
        private final ScheduledFuture<?> transitionToHalfOpenFuture;

        OpenState(final int attempts, CircuitBreakerMetrics metrics) {
            this.attempts = attempts;
            // 下一次尝试的时间间隔长度
            final long waitDurationInMillis = circuitBreakerConfig.getWaitIntervalFunctionInOpenState().apply(attempts);
            this.retryAfterWaitDuration = clock.instant().plus(waitDurationInMillis, ChronoUnit.MILLIS);
            this.metrics = metrics;

            // 尝试调度一个延迟线程，切换到HALF_OPEN状态
            if(circuitBreakerConfig.isAutomaticTransitionFromOpenToHalfOpenEnabled()) {
                ScheduledExecutorService service = schedulerFactory.getScheduler();
                this.transitionToHalfOpenFuture = service.schedule(this::toHalfOpenState, waitDurationInMillis, TimeUnit.MILLISECONDS);
            }else {
                this.transitionToHalfOpenFuture = null;
            }
            this.isOpen = new AtomicBoolean(true);
        }

        @Override
        public boolean tryAcquirePermission() {
            if(clock.instant().isAfter(this.retryAfterWaitDuration)) {
                this.toHalfOpenState();
                // 注意这里的tryAcquirePermission()，调用的已经是半开状态的方法实现了
                boolean callPermitted = stateReference.get().tryAcquirePermission();
                if(!callPermitted) {
                    publishCallNotPermittedEvent();
                    this.metrics.onCallNotPermitted();
                }
                return callPermitted;
            }
            // 到这里说明还是OPEN状态，没到半开的时间点
            this.metrics.onCallNotPermitted();
            return false;
        }

        @Override
        public void acquirePermission() {
            if(!this.tryAcquirePermission())
                throw CallNotPermittedException.createCallNotPermittedException(CircuitBreakerStateMachine.this);
        }

        @Override
        public void releasePermission() {
            // nothing to do
        }

        @Override
        public void onSuccess(long duration, TimeUnit unit) {
            this.metrics.onSuccess(duration, unit);
        }

        @Override
        public void onError(long duration, TimeUnit unit, Throwable t) {
            this.metrics.onError(duration, unit);
        }

        @Override
        public int attempts() {
            return this.attempts;
        }

        @Override
        public State getState() {
            return CircuitBreaker.State.OPEN;
        }

        @Override
        public CircuitBreakerMetrics getMetrics() {
            return this.metrics;
        }

        @Override
        public void preTransitionHook() {
            this.cancelAutomaticTransitionToHalfOpen();
        }

        /**
         * 切换到HALF_OPEN状态
         */
        private synchronized void toHalfOpenState() {
            if(this.isOpen.compareAndSet(true, false))
                transitionToHalfOpenState();
        }

        private void cancelAutomaticTransitionToHalfOpen() {
            if(this.transitionToHalfOpenFuture != null && !this.transitionToHalfOpenFuture.isDone())
                this.transitionToHalfOpenFuture.cancel(true);
        }
    }

    private class DisabledState implements CircuitBreakerState {
        private final CircuitBreakerMetrics metrics;

        DisabledState() {
            this.metrics = CircuitBreakerMetrics.forDisabled(getCircuitBreakerConfig(), clock);
        }

        @Override
        public boolean tryAcquirePermission() {
            return true;
        }

        @Override
        public void acquirePermission() {
            // nothing to do
        }

        @Override
        public void releasePermission() {
            // nothing to do
        }

        @Override
        public void onSuccess(long duration, TimeUnit unit) {
            // nothing to do
        }

        @Override
        public void onError(long duration, TimeUnit unit, Throwable t) {
            // nothing to do
        }

        @Override
        public int attempts() {
            return 0;
        }

        @Override
        public State getState() {
            return CircuitBreaker.State.DISABLED;
        }

        @Override
        public CircuitBreakerMetrics getMetrics() {
            return this.metrics;
        }
    }

    private class MetricsOnlyState implements CircuitBreakerState {
        private final CircuitBreakerMetrics metrics;
        private final AtomicBoolean isFailureRateExceeded;
        private final AtomicBoolean isSlowCallRateExceeded;

        MetricsOnlyState() {
            this.metrics = CircuitBreakerMetrics.forMetricsOnly(getCircuitBreakerConfig(), clock);
            this.isFailureRateExceeded = new AtomicBoolean(false);
            this.isSlowCallRateExceeded = new AtomicBoolean(false);
        }

        @Override
        public boolean tryAcquirePermission() {
            return true;
        }

        @Override
        public void acquirePermission() {
            // nothing to do
        }

        @Override
        public void releasePermission() {
            // nothing to do
        }

        @Override
        public void onSuccess(long duration, TimeUnit unit) {
            this.checkIfThresholdsExceeded(this.metrics.onSuccess(duration, unit));
        }

        @Override
        public void onError(long duration, TimeUnit unit, Throwable t) {
            this.checkIfThresholdsExceeded(this.metrics.onError(duration, unit));
        }

        private void checkIfThresholdsExceeded(CircuitBreakerMetrics.Result result) {
            if(!CircuitBreakerMetrics.Result.hasExceededThresholds(result)) // 都没超过阈值
                return;
            if(this.shouldPublishFailureRateExceededEvent(result))
                publishCircuitFailureRateExceededEvent(getName(), this.metrics.getFailureRate());
            if(this.shouldPublishSlowCallRateExceededEvent(result))
                publishCircuitSlowCallRateExceededEvent(getName(), this.metrics.getSlowCallRate());
        }

        private boolean shouldPublishFailureRateExceededEvent(CircuitBreakerMetrics.Result result) {
            return CircuitBreakerMetrics.Result.hasFailureRateExceededThreshold(result)
                    && this.isFailureRateExceeded.compareAndSet(false, true);
        }

        private boolean shouldPublishSlowCallRateExceededEvent(CircuitBreakerMetrics.Result result) {
            return CircuitBreakerMetrics.Result.hasSlowCallRateExceededThreshold(result)
                    && this.isSlowCallRateExceeded.compareAndSet(false, true);
        }

        @Override
        public int attempts() {
            return 0;
        }

        @Override
        public State getState() {
            return CircuitBreaker.State.METRICS_ONLY;
        }

        @Override
        public CircuitBreakerMetrics getMetrics() {
            return this.metrics;
        }
    }

    private class ForcedOpenState implements CircuitBreakerState {
        /** 当前第几个熔断周期，不是spring retry里面的调用次数 */
        private final int attempts;
        private final CircuitBreakerMetrics metrics;

        ForcedOpenState(final int attempts) {
            this.attempts = attempts;
            this.metrics = CircuitBreakerMetrics.forForcedOpen(circuitBreakerConfig, clock);
        }

        @Override
        public boolean tryAcquirePermission() {
            this.metrics.onCallNotPermitted();
            return false;
        }

        @Override
        public void acquirePermission() {
            this.metrics.onCallNotPermitted();
            throw CallNotPermittedException.createCallNotPermittedException(CircuitBreakerStateMachine.this);
        }

        @Override
        public void releasePermission() {
            // nothing to do
        }

        @Override
        public void onSuccess(long duration, TimeUnit unit) {
            // nothing to do
        }

        @Override
        public void onError(long duration, TimeUnit unit, Throwable t) {
            // nothing to do
        }

        @Override
        public int attempts() {
            return this.attempts;
        }

        @Override
        public State getState() {
            return CircuitBreaker.State.FORCED_OPEN;
        }

        @Override
        public CircuitBreakerMetrics getMetrics() {
            return this.metrics;
        }
    }

    private class HalfOpenState implements CircuitBreakerState {

        /** 允许的调用次数 */
        private final AtomicInteger permittedNumberOfCalls;

        /** 状态是否为半开 */
        private final AtomicBoolean isHalfOpen;

        /** 当前第几个熔断周期，不是spring retry里面的调用次数 */
        private final int attempts;

        private final CircuitBreakerMetrics metrics;

        /** 切换到打开状态的延迟线程调用结果 */
        @Nullable
        private final ScheduledFuture<?> transitionToOpenFuture;

        HalfOpenState(int attempts) {
            int permittedNumberOfCallsInHalfOpenState = circuitBreakerConfig.getPermittedNumberOfCallsInHalfOpenState();
            this.metrics = CircuitBreakerMetrics.forHalfOpen(permittedNumberOfCallsInHalfOpenState, getCircuitBreakerConfig(), clock);
            this.permittedNumberOfCalls = new AtomicInteger(permittedNumberOfCallsInHalfOpenState);
            this.isHalfOpen = new AtomicBoolean(true);
            this.attempts = attempts;

            // 尝试切回到打开状态的等待时间
            final long maxWaitDurationInHalfOpenState = circuitBreakerConfig.getMaxWaitDurationInHalfOpenState().toMillis();
            if(maxWaitDurationInHalfOpenState >= 1) {
                ScheduledExecutorService service = schedulerFactory.getScheduler();
                this.transitionToOpenFuture = service.schedule(this::toOpenState, maxWaitDurationInHalfOpenState, TimeUnit.MILLISECONDS);
            }else {
                this.transitionToOpenFuture = null;
            }
        }

        @Override
        public boolean tryAcquirePermission() {
            // 在规定次数内，可以访问
            if(this.permittedNumberOfCalls.getAndUpdate(current -> current == 0 ? current : --current) > 0)
               return true;
            // 到这里说明规定次数已用完
            this.metrics.onCallNotPermitted();
            return false;
        }

        @Override
        public void acquirePermission() {
            if(!this.tryAcquirePermission()) {
                throw CallNotPermittedException.createCallNotPermittedException(CircuitBreakerStateMachine.this);
            }
        }

        @Override
        public void preTransitionHook() {
            this.cancelAutomaticTransitionToOpen();
        }

        private void cancelAutomaticTransitionToOpen() {
            if(this.transitionToOpenFuture != null && !this.transitionToOpenFuture.isDone())
                this.transitionToOpenFuture.cancel(true);
        }

        @Override
        public void releasePermission() {
            this.permittedNumberOfCalls.incrementAndGet();
        }

        @Override
        public void onSuccess(long duration, TimeUnit unit) {
            this.checkIfThresholdsExceeded(this.metrics.onSuccess(duration, unit));
        }

        @Override
        public void onError(long duration, TimeUnit unit, Throwable t) {
            this.checkIfThresholdsExceeded(this.metrics.onError(duration, unit));
        }

        private void checkIfThresholdsExceeded(CircuitBreakerMetrics.Result result) {
            if(CircuitBreakerMetrics.Result.hasExceededThresholds(result)) {
                if(isHalfOpen.compareAndSet(true, false))
                    transitionToOpenState();
            }

            if(result == CircuitBreakerMetrics.Result.BELOW_THRESHOLDS) {
                // 注意是在这里切换到了CLOSED状态了
                if(isHalfOpen.compareAndSet(true, false))
                    transitionToClosedState();
            }
        }

        @Override
        public int attempts() {
            return this.attempts;
        }

        @Override
        public State getState() {
            return CircuitBreaker.State.HALF_OPEN;
        }

        @Override
        public CircuitBreakerMetrics getMetrics() {
            return this.metrics;
        }

        /**
         * 切换到HALF_OPEN状态
         */
        private synchronized void toOpenState() {
            if(this.isHalfOpen.compareAndSet(true, false))
                transitionToOpenState();
        }
    }
}
