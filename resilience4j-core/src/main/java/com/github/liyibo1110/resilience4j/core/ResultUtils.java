package com.github.liyibo1110.resilience4j.core;

import io.vavr.control.Either;

import java.util.function.Function;

/**
 * @author liyibo
 * @date 2026-02-02 18:04
 */
public final class ResultUtils {

    private ResultUtils() {}

    /**
     * 判断一个Either是否正常完成（注意左值是异常，右值才是正常）
     */
    public static <T> boolean isSuccessfulAndReturned(Either<? extends Throwable, ?> callsResult,
                                                      Class<T> expectedClass,
                                                      Function<T, Boolean> returnedChecker) {
        if(callsResult.isLeft())    // 结果是异常直接算失败
            return false;
        Object result = callsResult.get();  // 结果是null也算失败
        if(result == null)
            return false;
        if(!expectedClass.isAssignableFrom(result.getClass()))  // 结果类型和预期不符合，也算失败
            return false;
        // 最后还得由Function来做最终判定
        return returnedChecker.apply((T)result);
    }

    public static <T extends Throwable> boolean  isFailedAndThrown(Either<? extends Throwable, ?> callsResult,
                                                                   Class<T> expectedClass) {
        return isFailedAndThrown(callsResult, expectedClass, thrown -> true);
    }

    /**
     * 判断一个Either是否失败或抛出了异常（注意左值是异常，右值才是正常）
     */
    public static <T extends Throwable> boolean isFailedAndThrown(Either<? extends Throwable, ?> callsResult,
                                                                  Class<T> expectedClass,
                                                                  Function<T, Boolean> thrownChecker) {
        if(callsResult.isRight())   // 正常返回则算失败
            return false;
        Throwable thrown = callsResult.getLeft();
        if(!expectedClass.isAssignableFrom(thrown.getClass()))  // 结果类型和预期不符合，也算失败
            return false;
        // 最后还得由Function来做最终判定
        return thrownChecker.apply((T)thrown);
    }
}
