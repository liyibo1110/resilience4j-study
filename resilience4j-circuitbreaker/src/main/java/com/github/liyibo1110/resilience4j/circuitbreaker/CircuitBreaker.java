package com.github.liyibo1110.resilience4j.circuitbreaker;

import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnCallNotPermittedEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnFailureRateExceededEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnIgnoredErrorEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnResetEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnSlowCallRateExceededEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerOnSuccessEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.internal.CircuitBreakerStateMachine;
import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.functions.OnceConsumer;
import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedRunnable;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 线程安全的CircuitBreaker实例，可用于装饰多个请求。
 * 负责管理后端系统的状态，CircuitBreaker共有5个状态（关闭、打开、半开、禁用和强制打开），
 * 本身它并不了解后端的状态，而是使用装饰器通过onSuccess和onError事件提供的信息。
 * 在与后端通信之前，必须通过tryAcquirePermission()方法获得相应的权限。
 *
 * 当错误异常达到或超过特定阈值时，状态会从关闭 -> 打开，随后在一段持续时间内，所有对后端的访问会被拒绝。
 * 在经过了一段时间后，状态会从打开 -> 半开，并允许进行一定数量的调用，已查看后端是否可用，
 * 如果失败率大于或等于特定阈值，状态会变回打开，否则状态变回关闭。
 * @author liyibo
 * @date 2026-02-05 00:44
 */
public interface CircuitBreaker {

    /**
     * 装饰给定的CheckedFunction0（相当于Supplier）
     */
    static <T> CheckedFunction0<T> decorateCheckedSupplier(CircuitBreaker cb, CheckedFunction0<T> supplier) {
        return () -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                T result = supplier.apply();
                long duration = cb.getCurrentTimestamp() - start;
                cb.onResult(duration, cb.getTimestampUnit(), result);
                return result;
            } catch (Exception e) {
                // 注意不能处理Error
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;    // 上面都是增强功能，异常还是要继续抛出
            }
        };
    }

    static <T> Supplier<CompletionStage<T>> decorateCompletionStage(CircuitBreaker cb, Supplier<CompletionStage<T>> supplier) {
        return () -> {
            final CompletableFuture<T> promise = new CompletableFuture<>();
            if(!cb.tryAcquirePermission())
                promise.completeExceptionally(CallNotPermittedException.createCallNotPermittedException(cb));
            else {
                final long start = cb.getCurrentTimestamp();
                try {
                    supplier.get().whenComplete((result, throwable) -> {
                        long duration = cb.getCurrentTimestamp() - start;
                        if(throwable != null) {
                            if(throwable instanceof Exception) {
                                cb.onError(duration, cb.getTimestampUnit(), throwable);
                            }
                            promise.completeExceptionally(throwable);
                        }else {
                            cb.onResult(duration, cb.getTimestampUnit(), result);
                            promise.complete(result);
                        }
                    });
                } catch (Exception e) {
                    long duration = cb.getCurrentTimestamp() - start;
                    cb.onError(duration, cb.getTimestampUnit(), e);
                    promise.completeExceptionally(e);
                }
            }

            return promise;
        };
    }

    static CheckedRunnable decorateCheckedRunnable(CircuitBreaker cb, CheckedRunnable runnable) {
        return () -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                runnable.run();
                long duration = cb.getCurrentTimestamp() - start;
                cb.onSuccess(duration, cb.getTimestampUnit());
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static <T> Callable<T> decorateCallable(CircuitBreaker cb, Callable<T> callable) {
        return () -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                T result = callable.call();
                long duration = cb.getCurrentTimestamp() - start;
                cb.onResult(duration, cb.getTimestampUnit(), result);
                return result;
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static <T> Supplier<T> decorateSupplier(CircuitBreaker cb, Supplier<T> supplier) {
        return () -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                T result = supplier.get();
                long duration = cb.getCurrentTimestamp() - start;
                cb.onResult(duration, cb.getTimestampUnit(), result);
                return result;
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static <T> Supplier<Either<Exception, T>> decorateEitherSupplier(CircuitBreaker cb, Supplier<Either<? extends Exception, T>> supplier) {
        return () -> {
            if(cb.tryAcquirePermission()) {
                final long start = cb.getCurrentTimestamp();
                Either<? extends Exception, T> result = supplier.get();
                long duration = cb.getCurrentTimestamp() - start;
                if(result.isRight())
                    cb.onResult(duration, cb.getTimestampUnit(), result);
                else {
                    Exception e = result.getLeft();
                    cb.onError(duration, cb.getTimestampUnit(), e);
                }
                return Either.narrow(result);
            }else {
                return Either.left(CallNotPermittedException.createCallNotPermittedException(cb));
            }
        };
    }

    static <T> Supplier<Try<T>> decorateTrySupplier(CircuitBreaker cb, Supplier<Try<T>> supplier) {
        return () -> {
            if(cb.tryAcquirePermission()) {
                final long start = cb.getCurrentTimestamp();
                Try<T> result = supplier.get();
                long duration = cb.getCurrentTimestamp() - start;
                if(result.isSuccess())
                    cb.onResult(duration, cb.getTimestampUnit(), result);
                else
                    cb.onError(duration, cb.getTimestampUnit(), result.getCause());
                return result;
            }else {
                return Try.failure(CallNotPermittedException.createCallNotPermittedException(cb));
            }
        };
    }

    static <T> Consumer<T> decorateConsumer(CircuitBreaker cb, Consumer<T> consumer) {
        return (t) -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                consumer.accept(t);
                long duration = cb.getCurrentTimestamp() - start;
                cb.onSuccess(duration, cb.getTimestampUnit());
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static <T> CheckedConsumer<T> decorateCheckedConsumer(CircuitBreaker cb, CheckedConsumer<T> consumer) {
        return (t) -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                consumer.accept(t);
                long duration = cb.getCurrentTimestamp() - start;
                cb.onSuccess(duration, cb.getTimestampUnit());
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static Runnable decorateRunnable(CircuitBreaker cb, Runnable runnable) {
        return () -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                runnable.run();
                long duration = cb.getCurrentTimestamp() - start;
                cb.onSuccess(duration, cb.getTimestampUnit());
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static <T, R> Function<T, R> decorateFunction(CircuitBreaker cb, Function<T, R> function) {
        return (T t) -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                R returnValue = function.apply(t);
                long duration = cb.getCurrentTimestamp() - start;
                cb.onResult(duration, cb.getTimestampUnit(), returnValue);
                return returnValue;
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static <T, R> CheckedFunction1<T, R> decorateFunction(CircuitBreaker cb, CheckedFunction1<T, R> function) {
        return (T t) -> {
            cb.acquirePermission();
            final long start = cb.getCurrentTimestamp();
            try {
                R returnValue = function.apply(t);
                long duration = cb.getCurrentTimestamp() - start;
                cb.onResult(duration, cb.getTimestampUnit(), returnValue);
                return returnValue;
            } catch (Exception e) {
                long duration = cb.getCurrentTimestamp() - start;
                cb.onError(duration, cb.getTimestampUnit(), e);
                throw e;
            }
        };
    }

    static CircuitBreaker ofDefaults(String name) {
        return new CircuitBreakerStateMachine(name);
    }

    static CircuitBreaker of(String name, CircuitBreakerConfig config) {
        return new CircuitBreakerStateMachine(name, config);
    }

    static CircuitBreaker of(String name, CircuitBreakerConfig config,
                             io.vavr.collection.Map<String, String> tags) {
        return new CircuitBreakerStateMachine(name, config, tags);
    }

    static CircuitBreaker of(String name, Supplier<CircuitBreakerConfig> configSupplier) {
        return new CircuitBreakerStateMachine(name, configSupplier);
    }

    static CircuitBreaker of(String name, Supplier<CircuitBreakerConfig> configSupplier,
                             io.vavr.collection.Map<String, String> tags) {
        return new CircuitBreakerStateMachine(name, configSupplier, tags);
    }

    static <T> Supplier<Future<T>> decorateFuture(CircuitBreaker cb, Supplier<Future<T>> supplier) {
        return () -> {
            if(cb.tryAcquirePermission()) {
                final long start = cb.getCurrentTimestamp();
                try {
                    return new CircuitBreakerFuture<>(cb, supplier.get(), start);
                } catch (Exception e) {
                    long duration = cb.getCurrentTimestamp() - start;
                    cb.onError(duration, cb.getTimestampUnit(), e);
                    throw e;
                }
            }else {
                CompletableFuture<T> promise = new CompletableFuture<>();
                promise.completeExceptionally(CallNotPermittedException.createCallNotPermittedException(cb));
                return promise;
            }
        };
    }

    /**
     * 检查是否有执行权限，如果不允许调用，则不允许调用次数会增加。
     * 当状态为“开启”或“强制开启”，返回false。
     * 当状态为“关闭”或“禁用”，返回true。
     * 当状态为“半开”且允许进一步调用时，返回true，但调用次数会减少，如果已达到调用次数限制，返回false。
     */
    boolean tryAcquirePermission();

    /**
     * 释放一个permission，仅在已获取permission但未使用的时候才使用，
     * 否则应该使用onSuccess或onError来指示call完成或失败。
     * 如果状态为“半开”，则允许的call次数增加1次。
     */
    void releasePermission();

    /**
     * 尝试获取执行call的权限，如果不允许，则不允许call的计数会增加。
     * 当状态为“开启”或“强制开启”，会抛出CallNotPermittedException。
     * 当状态为“关闭”或“禁用”，正常返回。
     * 当状态为“半开”且允许进一步调用时，正常返回，但调用次数会减少，如果已达到调用次数限制，抛出CallNotPermittedException。
     * 重要提示：确保在调用结束后调用onSuccess或onError，如果调用在运行之前被取消，则必须再次释放权限。
     */
    void acquirePermission();

    /**
     * 记录失败的call，当call调用失败后必须调用这个方法
     */
    void onError(long duration, TimeUnit unit, Throwable t);

    /**
     * 记录成功的call，当call调用成功后必须调用这个方法
     */
    void onSuccess(long duration, TimeUnit unit);

    /**
     * 当得到call的调用返回值，必须调用这个方法，同时根据返回值自行决定是调用成功还是失败
     */
    void onResult(long duration, TimeUnit unit, Object result);

    /**
     * 使cb恢复到原始关闭状态，并清空统计数据，
     * 仅当希望完全重置cb而不创建新的cb时，才应该使用这个方法。
     */
    void reset();

    void transitionToClosedState();

    void transitionToOpenState();

    void transitionToHalfOpenState();

    void transitionToDisabledState();

    void transitionToMetricsOnlyState();

    void transitionToForcedOpenState();

    String getName();

    State getState();

    CircuitBreakerConfig getCircuitBreakerConfig();

    Metrics getMetrics();

    io.vavr.collection.Map<String, String> getTags();

    EventPublisher getEventPublisher();

    /**
     * 默认就是System.nanoTime()
     */
    long getCurrentTimestamp();

    /**
     * 默认就是TimeUnit.NANOSECONDS
     */
    TimeUnit getTimestampUnit();

    /**
     * 对外的干活方法，下面类似方法不再写注释了
     */
    default <T> T executeSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier).get();
    }

    default <T> Supplier<T> decorateSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier);
    }

    default <T> Either<Exception, T> executeEitherSupplier(Supplier<Either<? extends Exception, T>> supplier) {
        return decorateEitherSupplier(this, supplier).get();
    }

    default <T> Supplier<Try<T>> decorateTrySupplier(Supplier<Try<T>> supplier) {
        return decorateTrySupplier(this, supplier);
    }

    default <T> Try<T> executeTrySupplier(Supplier<Try<T>> supplier) {
        return decorateTrySupplier(this, supplier).get();
    }

    default <T> Supplier<Either<Exception, T>> decorateEitherSupplier(Supplier<Either<? extends Exception, T>> supplier) {
        return decorateEitherSupplier(this, supplier);
    }

    default <T> T executeCallable(Callable<T> callable) throws Exception {
        return decorateCallable(this, callable).call();
    }

    default <T> Callable<T> decorateCallable(Callable<T> callable) {
        return decorateCallable(this, callable);
    }

    default void executeRunnable(Runnable runnable) {
        decorateRunnable(this, runnable).run();
    }

    default Runnable decorateRunnable(Runnable runnable) {
        return decorateRunnable(this, runnable);
    }

    default <T> CompletionStage<T> executeCompletionStage(Supplier<CompletionStage<T>> supplier) {
        return decorateCompletionStage(this, supplier).get();
    }

    default <T> Supplier<CompletionStage<T>> decorateCompletionStage(Supplier<CompletionStage<T>> supplier) {
        return decorateCompletionStage(this, supplier);
    }

    default <T> T executeCheckedSupplier(CheckedFunction0<T> checkedSupplier) throws Throwable {
        return decorateCheckedSupplier(this, checkedSupplier).apply();
    }

    default <T> CheckedFunction0<T> decorateCheckedSupplier(CheckedFunction0<T> checkedSupplier) {
        return decorateCheckedSupplier(this, checkedSupplier);
    }

    default CheckedRunnable decorateCheckedRunnable(CheckedRunnable runnable) {
        return decorateCheckedRunnable(this, runnable);
    }

    default void executeCheckedRunnable(CheckedRunnable runnable) throws Throwable {
        decorateCheckedRunnable(this, runnable).run();
    }

    default <T> Consumer<T> decorateConsumer(Consumer<T> consumer) {
        return decorateConsumer(this, consumer);
    }

    default <T> CheckedConsumer<T> decorateCheckedConsumer(CheckedConsumer<T> consumer) {
        return decorateCheckedConsumer(this, consumer);
    }

    default <T> Supplier<Future<T>> decorateFuture(Supplier<Future<T>> supplier) {
        return decorateFuture(this, supplier);
    }

    /**
     * cb的状态
     */
    enum State {
        CLOSED(0, true),
        OPEN(1, true),
        HALF_OPEN(2, true),
        DISABLED(3, false),
        FORCED_OPEN(4, false),
        METRICS_ONLY(5, true);

        private final int order;
        public final boolean allowPublish;

        State(int order, boolean allowPublish) {
            this.order = order;
            this.allowPublish = allowPublish;
        }

        public int getOrder() {
            return order;
        }
    }

    enum StateTransition {
        CLOSED_TO_CLOSED(State.CLOSED, State.CLOSED),
        CLOSED_TO_OPEN(State.CLOSED, State.OPEN),
        CLOSED_TO_DISABLED(State.CLOSED, State.DISABLED),
        CLOSED_TO_METRICS_ONLY(State.CLOSED, State.METRICS_ONLY),
        CLOSED_TO_FORCED_OPEN(State.CLOSED, State.FORCED_OPEN),
        HALF_OPEN_TO_HALF_OPEN(State.HALF_OPEN, State.HALF_OPEN),
        HALF_OPEN_TO_CLOSED(State.HALF_OPEN, State.CLOSED),
        HALF_OPEN_TO_OPEN(State.HALF_OPEN, State.OPEN),
        HALF_OPEN_TO_DISABLED(State.HALF_OPEN, State.DISABLED),
        HALF_OPEN_TO_METRICS_ONLY(State.HALF_OPEN, State.METRICS_ONLY),
        HALF_OPEN_TO_FORCED_OPEN(State.HALF_OPEN, State.FORCED_OPEN),
        OPEN_TO_OPEN(State.OPEN, State.OPEN),
        OPEN_TO_CLOSED(State.OPEN, State.CLOSED),
        OPEN_TO_HALF_OPEN(State.OPEN, State.HALF_OPEN),
        OPEN_TO_DISABLED(State.OPEN, State.DISABLED),
        OPEN_TO_METRICS_ONLY(State.OPEN, State.METRICS_ONLY),
        OPEN_TO_FORCED_OPEN(State.OPEN, State.FORCED_OPEN),
        FORCED_OPEN_TO_FORCED_OPEN(State.FORCED_OPEN, State.FORCED_OPEN),
        FORCED_OPEN_TO_CLOSED(State.FORCED_OPEN, State.CLOSED),
        FORCED_OPEN_TO_OPEN(State.FORCED_OPEN, State.OPEN),
        FORCED_OPEN_TO_DISABLED(State.FORCED_OPEN, State.DISABLED),
        FORCED_OPEN_TO_METRICS_ONLY(State.FORCED_OPEN, State.METRICS_ONLY),
        FORCED_OPEN_TO_HALF_OPEN(State.FORCED_OPEN, State.HALF_OPEN),
        DISABLED_TO_DISABLED(State.DISABLED, State.DISABLED),
        DISABLED_TO_CLOSED(State.DISABLED, State.CLOSED),
        DISABLED_TO_OPEN(State.DISABLED, State.OPEN),
        DISABLED_TO_FORCED_OPEN(State.DISABLED, State.FORCED_OPEN),
        DISABLED_TO_HALF_OPEN(State.DISABLED, State.HALF_OPEN),
        DISABLED_TO_METRICS_ONLY(State.DISABLED, State.METRICS_ONLY),
        METRICS_ONLY_TO_METRICS_ONLY(State.METRICS_ONLY, State.METRICS_ONLY),
        METRICS_ONLY_TO_CLOSED(State.METRICS_ONLY, State.CLOSED),
        METRICS_ONLY_TO_FORCED_OPEN(State.METRICS_ONLY, State.FORCED_OPEN),
        METRICS_ONLY_TO_DISABLED(State.METRICS_ONLY, State.DISABLED);

        private final State fromState;
        private final State toState;

        StateTransition(State fromState, State toState) {
            this.fromState = fromState;
            this.toState = toState;
        }

        private static final Map<Tuple2<State, State>, StateTransition> STATE_TRANSITION_MAP =
                Arrays.stream(StateTransition.values())
                      .collect(Collectors.toMap(v -> Tuple.of(v.fromState, v.toState), Function.identity()));

        /**
         * fromState + toState -> StateTransition
         */
        public static StateTransition transitionBetween(String name, State fromState, State toState) {
            final StateTransition stateTransition = STATE_TRANSITION_MAP.get(Tuple.of(fromState, toState));
            if(stateTransition == null)
                throw new IllegalStateTransitionException(name, fromState, toState);
            return stateTransition;
        }

        /**
         * 是否from和to状态一致
         */
        public static boolean isInternalTransition(final StateTransition transition) {
            return transition.getToState() == transition.getFromState();
        }

        public State getFromState() {
            return fromState;
        }

        public State getToState() {
            return toState;
        }

        @Override
        public String toString() {
            return String.format("State transition from %s to %s", fromState, toState);
        }
    }

    interface EventPublisher extends com.github.liyibo1110.resilience4j.core.EventPublisher<CircuitBreakerEvent> {
        EventPublisher onSuccess(EventConsumer<CircuitBreakerOnSuccessEvent> eventConsumer);
        EventPublisher onError(EventConsumer<CircuitBreakerOnErrorEvent> eventConsumer);
        EventPublisher onStateTransition(EventConsumer<CircuitBreakerOnStateTransitionEvent> eventConsumer);
        EventPublisher onReset(EventConsumer<CircuitBreakerOnResetEvent> eventConsumer);
        EventPublisher onIgnoredError(EventConsumer<CircuitBreakerOnIgnoredErrorEvent> eventConsumer);
        EventPublisher onCallNotPermitted(EventConsumer<CircuitBreakerOnCallNotPermittedEvent> eventConsumer);
        EventPublisher onFailureRateExceeded(EventConsumer<CircuitBreakerOnFailureRateExceededEvent> eventConsumer);
        EventPublisher onSlowCallRateExceeded(EventConsumer<CircuitBreakerOnSlowCallRateExceededEvent> eventConsumer);
    }

    interface Metrics {
        float getFailureRate();
        float getSlowCallRate();
        int getNumberOfSlowCalls();
        int getNumberOfSlowSuccessfulCalls();
        int getNumberOfSlowFailedCalls();
        int getNumberOfBufferedCalls();
        int getNumberOfFailedCalls();
        long getNumberOfNotPermittedCalls();
        int getNumberOfSuccessfulCalls();
    }

    /**
     * 增加了cb的功能
     */
    final class CircuitBreakerFuture<T> implements Future<T> {
        private final OnceConsumer<CircuitBreaker> onceToCircuitBreaker;
        private final Future<T> future;
        private final long start;

        CircuitBreakerFuture(CircuitBreaker circuitBreaker, Future<T> future, long start) {
            Objects.requireNonNull(future, "Non null Future is required to decorate");
            this.onceToCircuitBreaker = OnceConsumer.of(circuitBreaker);
            this.future = future;
            this.start = start;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return this.future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return this.future.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            try {
                T v = this.future.get();
                onceToCircuitBreaker.applyOnce(cb -> cb.onResult(cb.getCurrentTimestamp() - this.start, cb.getTimestampUnit(), v));
                return v;
            } catch (CancellationException | InterruptedException e) {
                // 特殊的异常直接跳过
                onceToCircuitBreaker.applyOnce(cb -> cb.releasePermission());
                throw e;
            } catch (Exception e) {
                onceToCircuitBreaker.applyOnce(cb -> cb.onError(cb.getCurrentTimestamp() - start, cb.getTimestampUnit(), e));
                throw e;
            }
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                T v = this.future.get(timeout, unit);
                onceToCircuitBreaker.applyOnce(cb -> cb.onResult(cb.getCurrentTimestamp() - this.start, cb.getTimestampUnit(), v));
                return v;
            } catch (CancellationException | InterruptedException e) {
                // 特殊的异常直接跳过
                onceToCircuitBreaker.applyOnce(cb -> cb.releasePermission());
                throw e;
            } catch (Exception e) {
                onceToCircuitBreaker.applyOnce(cb -> cb.onError(cb.getCurrentTimestamp() - start, cb.getTimestampUnit(), e));
                throw e;
            }
        }
    }
}
