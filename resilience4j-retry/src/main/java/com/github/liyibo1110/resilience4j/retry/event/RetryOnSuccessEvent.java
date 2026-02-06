package com.github.liyibo1110.resilience4j.retry.event;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

/**
 * @author liyibo
 * @date 2026-02-06 11:50
 */
public class RetryOnSuccessEvent extends AbstractRetryEvent {

    public RetryOnSuccessEvent(String name, int currentNumOfAttempts, @Nullable Throwable lastThrowable) {
        super(name, currentNumOfAttempts, lastThrowable);
    }

    @Override
    public Type getEventType() {
        return Type.SUCCESS;
    }

    @Override
    public String toString() {
        return String.format(
                "%s: Retry '%s' recorded a successful retry attempt. Number of retry attempts: '%d', Last exception was: '%s'.",
                getCreationTime(), getName(), getNumberOfRetryAttempts(), getLastThrowable());
    }
}
