package com.github.liyibo1110.resilience4j.retry.event;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.time.Duration;

/**
 * @author liyibo
 * @date 2026-02-06 11:53
 */
public class RetryOnRetryEvent extends AbstractRetryEvent {
    private final Duration waitInterval;

    public RetryOnRetryEvent(String name, int numberOfAttempts, @Nullable Throwable lastThrowable, long waitInterval) {
        super(name, numberOfAttempts, lastThrowable);
        this.waitInterval = Duration.ofMillis(waitInterval);
    }

    @Override
    public Type getEventType() {
        return Type.RETRY;
    }

    public Duration getWaitInterval() {
        return waitInterval;
    }

    @Override
    public String toString() {
        return String.format("%s: Retry '%s', waiting %s until attempt '%d'. Last attempt failed with exception '%s'.",
                getCreationTime(), getName(), getWaitInterval(), getNumberOfRetryAttempts(), getLastThrowable());
    }
}
