package com.github.liyibo1110.resilience4j.timelimiter.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-08 18:38
 */
public abstract class AbstractTimeLimiterEvent implements TimeLimiterEvent {
    private final String timeLimiterName;
    private final ZonedDateTime creationTime;
    private Type eventType;

    AbstractTimeLimiterEvent(String timeLimiterName, Type eventType) {
        this.timeLimiterName = timeLimiterName;
        this.creationTime = ZonedDateTime.now();
        this.eventType = eventType;
    }

    @Override
    public String getTimeLimiterName() {
        return timeLimiterName;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public Type getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "TimeLimiterEvent{" +
                "type=" + getEventType() +
                ", timeLimiterName='" + getTimeLimiterName() + '\'' +
                ", creationTime=" + getCreationTime() +
                '}';
    }
}
