package com.github.liyibo1110.resilience4j.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 用来把ThreadLocal等上下文实例，进行跨线程传播的的抽象（Reactor/线程池模块会用到）
 * @param <T> 跨线程复制的值的类型
 * @author liyibo
 * @date 2026-02-02 18:16
 */
public interface ContextPropagator<T> {

    /**
     * 从当前线程检索值，此方法会生成将要传给新线程的值
     */
    Supplier<Optional<T>> retrieve();

    /**
     * 将父线程中的值复制到新执行的线程中，此方法接收的是父线程中retrieve()方法返回的值
     */
    Consumer<Optional<T>> copy();

    /**
     * 在线程执行完成前清理值，此方法接收的是从父线程中retrieve()方法返回的值
     */
    Consumer<Optional<T>> clear();

    /**
     * 装饰给定的Supplier，提供跨线程复制变量的功能
     */
    static <T> Supplier<T> decorateSupplier(ContextPropagator propagator, Supplier<T> supplier) {
        final Optional value = (Optional)propagator.retrieve().get();
        return () -> {
            try {
                propagator.copy().accept(value);
                return supplier.get();
            } finally {
                propagator.clear().accept(value);
            }
        };
    }

    static <T> Supplier<T> decorateSupplier(List<? extends ContextPropagator> propagators, Supplier<T> supplier) {
        Objects.requireNonNull(propagators, "ContextPropagator list should be non null");
        // key是ContextPropagator实例，value是retrieve()的返回值
        final Map<? extends ContextPropagator, Object> values = propagators.stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> p.retrieve().get(),
                        (first, second) -> second,
                        HashMap::new));
        return () -> {
            try {
                values.forEach((p, v) -> p.copy().accept(v));
                return supplier.get();
            } finally {
                values.forEach((p, v) -> p.clear().accept(v));
            }
        };
    }

    /**
     * 装饰给定的Callable，提供跨线程复制变量的功能
     */
    static <T> Callable<T> decorateCallable(ContextPropagator propagator, Callable<T> callable) {
        final Optional value = (Optional) propagator.retrieve().get();
        return () -> {
            try {
                propagator.copy().accept(value);
                return callable.call();
            } finally {
                propagator.clear().accept(value);
            }
        };
    }

    static <T> Callable<T> decorateCallable(List<? extends ContextPropagator> propagators, Callable<T> callable) {
        Objects.requireNonNull(propagators, "ContextPropagator list should be non null");
        // key是ContextPropagator实例，value是retrieve()的返回值
        final Map<? extends ContextPropagator, Object> values = propagators.stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> p.retrieve().get(),
                        (first, second) -> second,
                        HashMap::new));
        return () -> {
            try {
                values.forEach((p, v) -> p.copy().accept(v));
                return callable.call();
            } finally {
                values.forEach((p, v) -> p.clear().accept(v));
            }
        };
    }

    /**
     * 装饰给定的Runnable，提供跨线程复制变量的功能
     */
    static <T> Runnable decorateRunnable(ContextPropagator propagator, Runnable runnable) {
        final Optional value = (Optional) propagator.retrieve().get();
        return () -> {
            try {
                propagator.copy().accept(value);
                runnable.run();
            } finally {
                propagator.clear().accept(value);
            }
        };
    }

    static <T> Runnable decorateRunnable(List<? extends ContextPropagator> propagators, Runnable runnable) {
        Objects.requireNonNull(propagators, "ContextPropagator list should be non null");
        // key是ContextPropagator实例，value是retrieve()的返回值
        final Map<? extends ContextPropagator, Object> values = propagators.stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> p.retrieve().get(),
                        (first, second) -> second,
                        HashMap::new));
        return () -> {
            try {
                values.forEach((p, v) -> p.copy().accept(v));
                runnable.run();
            } finally {
                values.forEach((p, v) -> p.clear().accept(v));
            }
        };
    }

    /**
     * 空的ContextPropagator实现类
     */
    class EmptyContextPropagator<T> implements ContextPropagator<T> {
        @Override
        public Supplier<Optional<T>> retrieve() {
            return () -> Optional.empty();
        }

        @Override
        public Consumer<Optional<T>> copy() {
            return t -> {};
        }

        @Override
        public Consumer<Optional<T>> clear() {
            return t -> {};
        }
    }
}
