package com.github.liyibo1110.resilience4j.retry.event;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

/**
 * @author liyibo
 * @date 2026-02-06 11:51
 */
public class RetryOnErrorEvent extends AbstractRetryEvent {

    public RetryOnErrorEvent(String name, int numberOfAttempts, @Nullable Throwable lastThrowable) {
        super(name, numberOfAttempts, lastThrowable);
    }

    @Override
    public Type getEventType() {
        return Type.ERROR;
    }

    @Override
    public String toString() {
        return String.format(
                "%s: Retry '%s' recorded a failed retry attempt. Number of retry attempts: '%d'. Giving up. Last exception was: '%s'.",
                getCreationTime(), getName(), getNumberOfRetryAttempts(), getLastThrowable());
    }
}
