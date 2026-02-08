package com.github.liyibo1110.resilience4j.bulkhead.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-08 23:00
 */
public interface BulkheadEvent {
    String getBulkheadName();
    Type getEventType();
    ZonedDateTime getCreationTime();

    enum Type {
        CALL_PERMITTED,
        CALL_REJECTED,
        CALL_FINISHED
    }
}
