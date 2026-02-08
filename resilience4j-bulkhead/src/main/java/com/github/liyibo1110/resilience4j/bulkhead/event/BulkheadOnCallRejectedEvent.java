package com.github.liyibo1110.resilience4j.bulkhead.event;

/**
 * @author liyibo
 * @date 2026-02-08 23:02
 */
public class BulkheadOnCallRejectedEvent extends AbstractBulkheadEvent {

    public BulkheadOnCallRejectedEvent(String bulkheadName) {
        super(bulkheadName);
    }

    @Override
    public Type getEventType() {
        return Type.CALL_REJECTED;
    }

    @Override
    public String toString() {
        return String.format("%s: Bulkhead '%s' rejected a call.",
                getCreationTime(), getBulkheadName());
    }
}
