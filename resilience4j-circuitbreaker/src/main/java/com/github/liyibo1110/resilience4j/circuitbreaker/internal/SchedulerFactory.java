package com.github.liyibo1110.resilience4j.circuitbreaker.internal;

import io.vavr.Lazy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 一个单线程的Schedule线程池的构造工厂（单例并且延迟生成），用来执行特定的守护线程（负责CircuitBreaker状态迁移）
 * @author liyibo
 * @date 2026-02-05 14:40
 */
public class SchedulerFactory {
    private static Lazy<SchedulerFactory> lazyInstance = Lazy.of(SchedulerFactory::new);

    private Lazy<ScheduledExecutorService> lazyScheduler = Lazy
            .of(() -> Executors.newSingleThreadScheduledExecutor(threadTask -> {
                Thread t = new Thread(threadTask, "CircuitBreakerAutoTransitionThread");
                t.setDaemon(true);
                return t;
            }));

    private SchedulerFactory() {}

    public static SchedulerFactory getInstance() {
        return lazyInstance.get();
    }

    public ScheduledExecutorService getScheduler() {
        return lazyScheduler.get();
    }
}
