package com.github.liyibo1110.resilience4j.ratelimiter.internal;

import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnDrainedEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnFailureEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnSuccessEvent;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 将从纪元开始以来的所有纳秒数拆分为周期，每个周期持续时间为limitRefreshPeriod纳秒。
 * 根据每个开始的合约，将State.activePermissions设置为limitForPeriod。
 * 所有更新都是原子性的，并且状态被封装在AtomicReference中。
 * 和SemaphoreBasedRateLimiter相比，核心变化是：来了请求，会根据当前时间推算“理论上应该剩多少许可”，然后原子性更新状态，
 * 不需要额外刷新配额，完全按照时间来推算配额。
 * @author liyibo
 * @date 2026-02-06 23:33
 */
public class AtomicRateLimiter implements RateLimiter {
    /** AtomicRateLimiter实例的生成时间点 */
    private final long nanoTimeStart;
    private final String name;

    /** 等候许可的线程总数，只用来做统计 */
    private final AtomicInteger waitingThreads;

    /** 内置了状态对象（3个字段） */
    private final AtomicReference<State> state;
    private final Map<String, String> tags;
    private final RateLimiterEventProcessor eventProcessor;

    public AtomicRateLimiter(String name, RateLimiterConfig config) {
        this(name, config, HashMap.empty());
    }

    public AtomicRateLimiter(String name, RateLimiterConfig config, Map<String, String> tags) {
        this.name = name;
        this.tags = tags;
        this.nanoTimeStart = System.nanoTime();
        this.waitingThreads = new AtomicInteger(0);
        this.state = new AtomicReference<>(new State(config, 0, config.getLimitForPeriod(), 0));
        this.eventProcessor = new RateLimiterEventProcessor();
    }

    @Override
    public void changeTimeoutDuration(Duration timeoutDuration) {
        RateLimiterConfig newConfig = RateLimiterConfig.from(state.get().config)
                .timeoutDuration(timeoutDuration)
                .build();
        this.state.updateAndGet(currentState -> new State(newConfig, currentState.activeCycle, currentState.activePermissions,
                currentState.nanosToWait));
    }

    @Override
    public void changeLimitForPeriod(int limitForPeriod) {
        RateLimiterConfig newConfig = RateLimiterConfig.from(state.get().config)
                .limitForPeriod(limitForPeriod)
                .build();
        this.state.updateAndGet(currentState -> new State(newConfig, currentState.activeCycle, currentState.activePermissions,
                currentState.nanosToWait));
    }

    /**
     * 计算从AtomicRateLimiter实例创建后，经过了多少时间
     */
    private long currentNanoTime() {
        return System.nanoTime() - this.nanoTimeStart;
    }

    long getNanoTimeStart() {
        return this.nanoTimeStart;
    }

    @Override
    public boolean acquirePermission(int permits) {
        // 获取许可的等待timeout
        long timeoutInNanos = this.state.get().config.getTimeoutDuration().toNanos();
        // 计算出下一个状态
        State modifiedState = this.updateStateWithBackOff(permits, timeoutInNanos);
        boolean result = this.waitForPermissionIfNecessary(timeoutInNanos, modifiedState.nanosToWait);
        this.publishRateLimiterAcquisitionEvent(result, permits);
        return result;
    }

    @Override
    public long reservePermission(int permits) {
        long timeoutInNanos = state.get().config.getTimeoutDuration().toNanos();
        State modifiedState = this.updateStateWithBackOff(permits, timeoutInNanos);

        boolean canAcquireImmediately = modifiedState.nanosToWait <= 0;
        if(canAcquireImmediately) {
            this.publishRateLimiterAcquisitionEvent(true, permits);
            return 0;
        }

        boolean canAcquireInTime = timeoutInNanos >= modifiedState.nanosToWait;
        if(canAcquireInTime) {
            this.publishRateLimiterAcquisitionEvent(true, permits);
            return modifiedState.nanosToWait;
        }

        // 等不到了
        this.publishRateLimiterAcquisitionEvent(false, permits);
        return -1;
    }

    @Override
    public void drainPermissions() {
        AtomicRateLimiter.State prev;
        AtomicRateLimiter.State next;
        do {
            prev = this.state.get();
            /**
             * 注意第一个参数activePermissions，本质就是这次要申请所有的剩余许可。
             * 同时第二个参数timeoutInNanos传入的是0，表示在reservePermissions方法中，只会扣除立即可用的许可。
             */
            next = this.calculateNextState(prev.activePermissions, 0, prev);
        } while(!this.compareAndSet(prev, next));
        if(this.eventProcessor.hasConsumers())
            this.eventProcessor.consumeEvent(new RateLimiterOnDrainedEvent(this.name, Math.min(prev.activePermissions, 0)));
    }

    /**
     * 使用计算出的下一个状态，来原子性地更新当前State，并返回更新的State。
     */
    private State updateStateWithBackOff(final int permits, final long timeoutInNanos) {
        AtomicRateLimiter.State prev;
        AtomicRateLimiter.State next;   // 下一个状态实例
        do {
            prev = this.state.get();
            next = this.calculateNextState(permits, timeoutInNanos, prev);
        } while(!this.compareAndSet(prev, next));   // compareAndSet可能会不成功，不成功则重新循环
        return next;
    }

    /**
     * 原子性的更新state字段里的State实例
     */
    private boolean compareAndSet(final State current, final State next) {
        if(this.state.compareAndSet(current, next))
            return true;
        LockSupport.parkNanos(1);   // 微退避，避免CAS竞争，属于高端优化的细节
        return false;
    }

    /**
     * 根据传来的3个参数值，计算下一次的状态
     */
    private State calculateNextState(final int permits, final long timeoutInNanos, final State activeState) {
        // 读取配置
        long cyclePeriodInNanos = activeState.config.getLimitRefreshPeriod().toNanos();
        int permissionsPerCycle = activeState.config.getLimitForPeriod();

        long currentNanos = this.currentNanoTime();
        // 时间周期推进算法重点：即现在的时间点，应该属于哪个cycle，计算方式如下代码
        long currentCycle = currentNanos / cyclePeriodInNanos;

        // 取出现在状态的旧值
        long nextCycle = activeState.activeCycle;
        int nextPermissions = activeState.activePermissions;
        if(nextCycle != currentCycle) { // 是否进入新的周期cycle，是则需要补发许可
            long elapsedCycles = currentCycle - nextCycle;  // 到底跨了多少个周期了
            long accumulatedPermissions = elapsedCycles * permissionsPerCycle;  // 计算需要补多少许可
            // 下一个状态的新值
            nextCycle = currentCycle;
            nextPermissions = (int)Math.min(nextPermissions + accumulatedPermissions, permissionsPerCycle); // 不能超过配置上限
        }
        // 计算下一次获取许可，要等待多久（也是个重要方法）
        long nextNanosToWait = this.nanosToWaitForPermission(permits, cyclePeriodInNanos, permissionsPerCycle,
                nextPermissions, currentNanos, currentCycle);
        State nextState = this.reservePermissions(activeState.config, permits, timeoutInNanos,
                                                nextCycle, nextPermissions, nextNanosToWait);
        return nextState;
    }

    /**
     * 核心算法：计算下一次获取许可，要等待的时间
     */
    private long nanosToWaitForPermission(final int permits, final long cyclePeriodInNanos, final int permissionsPerCycle,
                                          final int availablePermissions, final long currentNanos, final long currentCycle) {
        if(availablePermissions >= permits) // 额度足够，则不用等待
            return 0L;
        // 额度不够
        long nextCycleTimeInNanos = (currentCycle + 1) * cyclePeriodInNanos;
        long nanosToNextCycle = nextCycleTimeInNanos - currentNanos;    // 下轮的开始时间
        // 下轮开始时的可用额度，注意availablePermissions可能是个负数
        int permissionsAtTheStartOfNextCycle = availablePermissions + permissionsPerCycle;
        // 要等多少个周期，因为下一个周期的许可可能也不够用
        int fullCyclesToWait = divCeil(-(permissionsAtTheStartOfNextCycle - permits), permissionsPerCycle);
        return (fullCyclesToWait * cyclePeriodInNanos) + nanosToNextCycle;  // 最终值是总等待时间
    }

    /**
     * 将2个整数相除，结果向上取整
     */
    private static int divCeil(int x, int y) {
        return (x + y - 1) / y;
    }

    /**
     * 判断能否在超时前真的获取许可，并生成下一个State返回
     */
    private State reservePermissions(final RateLimiterConfig config, final int permits, final long timeoutInNanos,
                                     final long cycle, final int permissions, final long nanosToWait) {
        // 判断acquire传来最大等待时间，是否大于上一步计算出来的总等待时间
        boolean canAcquireInTime = timeoutInNanos >= nanosToWait;
        int permissionsWithReservation = permissions;
        if(canAcquireInTime)
            permissionsWithReservation -= permits;
        return new State(config, cycle, permissionsWithReservation, nanosToWait);
    }

    /**
     * 根据刚刚计算出来的新的nanosToWait（下一次要等待的时间），来决定是立即通过，还是阻塞等待或者等timeout后再失败
     */
    private boolean waitForPermissionIfNecessary(final long timeoutInNanos, final long nanosToWait) {
        boolean canAcquireImmediately = nanosToWait <= 0;
        boolean canAcquireInTime = timeoutInNanos >= nanosToWait;
        if(canAcquireImmediately)
            return true;
        if(canAcquireInTime)
            return this.waitForPermission(nanosToWait);
        this.waitForPermission(timeoutInNanos);
        return false;
    }

    /**
     * 核心阻塞实现（用的是lockSupport.parkNanos，而不是Thread的sleep）
     */
    private boolean waitForPermission(final long nanosToWait) {
        this.waitingThreads.incrementAndGet();
        long deadline = currentNanoTime() + nanosToWait;    // 绝对时间值
        boolean wasInterrupted = false;
        while(currentNanoTime() < deadline && !wasInterrupted) {
            long sleepBlockDuration = deadline - currentNanoTime();
            LockSupport.parkNanos(sleepBlockDuration);  // parkNanos引起的阻塞可能会被提前唤醒，因此要在循环里重新计算当次的阻塞时间
            wasInterrupted = Thread.interrupted();
        }
        this.waitingThreads.decrementAndGet();
        if(wasInterrupted)
            Thread.currentThread().interrupt();
        return !wasInterrupted;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RateLimiterConfig getRateLimiterConfig() {
        return state.get().config;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public Metrics getMetrics() {
        return new AtomicRateLimiterMetrics();
    }

    @Override
    public EventPublisher getEventPublisher() {
        return this.eventProcessor;
    }

    @Override
    public String toString() {
        return "AtomicRateLimiter{" +
                "name='" + name + '\'' +
                ", rateLimiterConfig=" + state.get().config +
                '}';
    }

    public AtomicRateLimiterMetrics getDetailedMetrics() {
        return new AtomicRateLimiterMetrics();
    }

    private void publishRateLimiterAcquisitionEvent(boolean permissionAcquired, int permits) {
        if(!this.eventProcessor.hasConsumers())
            return;
        if(permissionAcquired) {
            this.eventProcessor.consumeEvent(new RateLimiterOnSuccessEvent(this.name, permits));
            return;
        }
        this.eventProcessor.consumeEvent(new RateLimiterOnFailureEvent(this.name, permits));
    }

    /**
     * 表示这个RateLimiter实现类的状态
     */
    private static class State {
        private final RateLimiterConfig config;

        /** 上一次调用acquirePermission()时，使用的周期数，这个值如果变了，则许可将要恢复重置 */
        private final long activeCycle;

        /** 上一次调用acquirePermission()后，可用的许可数量，如果某些许可被预支，该值可能为负数 */
        private final int activePermissions;

        /** 上一次调用acquirePermission()时，需要等待获取许可的纳秒数 */
        private final long nanosToWait;

        private State(RateLimiterConfig config, final long activeCycle, final int activePermissions, final long nanosToWait) {
            this.config = config;
            this.activeCycle = activeCycle;
            this.activePermissions = activePermissions;
            this.nanosToWait = nanosToWait;
        }
    }

    public class AtomicRateLimiterMetrics implements Metrics {

        private AtomicRateLimiterMetrics() {}

        @Override
        public int getNumberOfWaitingThreads() {
            return waitingThreads.get();
        }

        @Override
        public int getAvailablePermissions() {
            State currentState = state.get();
            State estimatedState = calculateNextState(1, -1, currentState);
            return estimatedState.activePermissions;
        }

        public long getNanosToWait() {
            State currentState = state.get();
            State estimatedState = calculateNextState(1, -1, currentState);
            return estimatedState.nanosToWait;
        }

        public long getCycle() {
            State currentState = state.get();
            State estimatedState = calculateNextState(1, -1, currentState);
            return estimatedState.activeCycle;
        }
    }
}
