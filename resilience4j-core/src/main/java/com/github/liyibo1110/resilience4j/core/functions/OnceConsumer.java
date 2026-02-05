package com.github.liyibo1110.resilience4j.core.functions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 保证只计算一次的Consumer（即外部多次调用同一个实例的applyOnce，也只会执行一次）
 * @author liyibo
 * @date 2026-02-02 14:45
 */
public final class OnceConsumer<T> {
    final T t;
    private final AtomicBoolean hasRun = new AtomicBoolean(false);

    private OnceConsumer(T t) {
        this.t = t;
    }

    /**
     * 实际可用的构造方法
     */
    public static <T> OnceConsumer<T> of(T t) {
        return new OnceConsumer<>(t);
    }

    public void applyOnce(Consumer<T> consumer) {
        if(hasRun.compareAndSet(false, true))
            consumer.accept(t);
    }
}
