package com.github.liyibo1110.resilience4j.timelimiter.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-08 18:35
 */
public interface TimeLimiterEvent {

    String getTimeLimiterName();
    Type getEventType();
    ZonedDateTime getCreationTime();

    enum Type {
        SUCCESS,
        TIMEOUT,
        ERROR
    }
}
