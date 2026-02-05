package com.github.liyibo1110.resilience4j.core.predicate;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Throwable.class -> Predicate(Throwable)，这样的生成器，
 * 通俗一点说就是：把一组异常类型 -> 一个可执行的异常判断函数，
 * 比如传入IOException.class和TimeoutException.class，会生成一个Predicate(Throwable)，
 * 效果等价于e -> e instanceof IOException || e instanceof TimeoutException
 * @author liyibo
 * @date 2026-02-04 16:01
 */
public final class PredicateCreator {
    private PredicateCreator() {}

    @SafeVarargs
    public static Optional<Predicate<Throwable>> createExceptionsPredicate(Class<? extends Throwable>... recordExceptions) {
        return exceptionPredicate(recordExceptions);
    }

    @SafeVarargs
    public static Optional<Predicate<Throwable>> createNegatedExceptionsPredicate(Class<? extends Throwable>... ignoreExceptions) {
        return exceptionPredicate(ignoreExceptions).map(Predicate::negate);
    }

    private static Optional<Predicate<Throwable>> exceptionPredicate(Class<? extends Throwable>[] recordExceptions) {
        return Arrays.stream(recordExceptions)
                .distinct()
                .map(PredicateCreator::makePredicate)
                .reduce(Predicate::or);
    }

    /**
     * 生成Predicate对象，作用是检查是否匹配给定的clazz
     */
    private static Predicate<Throwable> makePredicate(Class<? extends Throwable> clazz) {
        return (Throwable e) -> clazz.isAssignableFrom(e.getClass());
    }
}
