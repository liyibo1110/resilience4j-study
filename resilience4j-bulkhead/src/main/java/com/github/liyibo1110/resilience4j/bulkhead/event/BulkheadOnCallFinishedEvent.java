package com.github.liyibo1110.resilience4j.bulkhead.event;

/**
 * @author liyibo
 * @date 2026-02-08 23:03
 */
public class BulkheadOnCallFinishedEvent extends AbstractBulkheadEvent {

    public BulkheadOnCallFinishedEvent(String bulkheadName) {
        super(bulkheadName);
    }

    @Override
    public Type getEventType() {
        return Type.CALL_FINISHED;
    }

    @Override
    public String toString() {
        return String.format("%s: Bulkhead '%s' has finished a call.",
                getCreationTime(), getBulkheadName());
    }
}
