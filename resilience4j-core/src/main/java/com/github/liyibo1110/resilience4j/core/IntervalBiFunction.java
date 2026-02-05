package com.github.liyibo1110.resilience4j.core;

import io.vavr.control.Either;

import java.util.function.BiFunction;

/**
 * 可用于计算等待间隔的Function。
 * 输入参数是尝试次数（attempt）和正常结果或抛出的异常，输出是等待间隔（毫秒），
 * 尝试次数从1开始，每次尝试后都会增加该值
 * @author liyibo
 * @date 2026-02-02 17:33
 */
@FunctionalInterface
public interface IntervalBiFunction<T> extends BiFunction<Integer, Either<Throwable, T>, Long> {
    static <T> IntervalBiFunction<T> ofIntervalFunction(IntervalFunction func) {
        // 就是做了一层适配转换
        return (attempt, either) -> func.apply(attempt);
    }
}
