package com.github.liyibo1110.resilience4j.bulkhead.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-08 23:01
 */
abstract class AbstractBulkheadEvent implements BulkheadEvent {

    private final String bulkheadName;
    private final ZonedDateTime creationTime;

    AbstractBulkheadEvent(String bulkheadName) {
        this.bulkheadName = bulkheadName;
        this.creationTime = ZonedDateTime.now();
    }

    @Override
    public String getBulkheadName() {
        return bulkheadName;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }
}
