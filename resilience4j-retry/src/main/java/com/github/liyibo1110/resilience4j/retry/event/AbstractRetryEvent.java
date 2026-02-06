package com.github.liyibo1110.resilience4j.retry.event;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-06 11:43
 */
abstract class AbstractRetryEvent implements RetryEvent {
    private final String name;
    private final ZonedDateTime creationTime;
    private final int numberOfAttempts;
    @Nullable
    private final Throwable lastThrowable;

    AbstractRetryEvent(String name, int numberOfAttempts, @Nullable Throwable lastThrowable) {
        this.name = name;
        this.numberOfAttempts = numberOfAttempts;
        this.creationTime = ZonedDateTime.now();
        this.lastThrowable = lastThrowable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public int getNumberOfRetryAttempts() {
        return numberOfAttempts;
    }

    @Override
    @Nullable
    public Throwable getLastThrowable() {
        return lastThrowable;
    }
}
