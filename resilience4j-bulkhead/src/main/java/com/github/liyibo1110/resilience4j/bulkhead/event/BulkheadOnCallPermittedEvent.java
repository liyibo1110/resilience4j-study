package com.github.liyibo1110.resilience4j.bulkhead.event;

/**
 * @author liyibo
 * @date 2026-02-08 23:01
 */
public class BulkheadOnCallPermittedEvent extends AbstractBulkheadEvent {

    public BulkheadOnCallPermittedEvent(String bulkheadName) {
        super(bulkheadName);
    }

    @Override
    public Type getEventType() {
        return Type.CALL_PERMITTED;
    }

    @Override
    public String toString() {
        return String.format("%s: Bulkhead '%s' permitted a call.",
                getCreationTime(), getBulkheadName());
    }
}
