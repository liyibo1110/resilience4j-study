package com.github.liyibo1110.resilience4j.retry.event;

import java.time.ZonedDateTime;

/**
 * retry模块专用的event接口
 * @author liyibo
 * @date 2026-02-06 11:39
 */
public interface RetryEvent {
    String getName();
    int getNumberOfRetryAttempts();
    Type getEventType();
    ZonedDateTime getCreationTime();
    Throwable getLastThrowable();

    enum Type {
        RETRY,  // call的重试
        ERROR,  // call经过N次重试后最终失败
        SUCCESS,    // call成功
        IGNORED_ERROR   // call失败但是可以被忽略
    }
}
