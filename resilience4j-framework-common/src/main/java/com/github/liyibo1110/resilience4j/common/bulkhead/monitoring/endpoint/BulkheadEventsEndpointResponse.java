package com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:43
 */
public class BulkheadEventsEndpointResponse {
    private List<BulkheadEventDTO> bulkheadEvents;

    public BulkheadEventsEndpointResponse() {}

    public BulkheadEventsEndpointResponse(List<BulkheadEventDTO> bulkheadEvents) {
        this.bulkheadEvents = bulkheadEvents;
    }

    public List<BulkheadEventDTO> getBulkheadEvents() {
        return bulkheadEvents;
    }

    public void setBulkheadEvents(List<BulkheadEventDTO> bulkheadEvents) {
        this.bulkheadEvents = bulkheadEvents;
    }
}
