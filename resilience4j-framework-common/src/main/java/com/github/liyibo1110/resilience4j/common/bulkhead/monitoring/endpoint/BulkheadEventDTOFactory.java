package com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:41
 */
public class BulkheadEventDTOFactory {
    private BulkheadEventDTOFactory() {}

    public static BulkheadEventDTO createBulkheadEventDTO(BulkheadEvent event) {
        switch (event.getEventType()) {
            case CALL_PERMITTED:
            case CALL_REJECTED:
            case CALL_FINISHED:
                return new BulkheadEventDTO(event.getBulkheadName(), event.getEventType(), event.getCreationTime().toString());
            default:
                throw new IllegalArgumentException("Invalid event");
        }
    }
}
