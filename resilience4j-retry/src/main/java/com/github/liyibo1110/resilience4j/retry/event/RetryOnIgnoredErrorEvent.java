package com.github.liyibo1110.resilience4j.retry.event;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

/**
 * @author liyibo
 * @date 2026-02-06 11:52
 */
public class RetryOnIgnoredErrorEvent extends AbstractRetryEvent {

    public RetryOnIgnoredErrorEvent(String name, @Nullable Throwable lastThrowable) {
        super(name, 0, lastThrowable);
    }

    @Override
    public Type getEventType() {
        return Type.IGNORED_ERROR;
    }

    @Override
    public String toString() {
        return String.format("%s: Retry '%s' recorded an error which has been ignored: '%s'.",
                getCreationTime(), getName(), getLastThrowable());
    }
}
