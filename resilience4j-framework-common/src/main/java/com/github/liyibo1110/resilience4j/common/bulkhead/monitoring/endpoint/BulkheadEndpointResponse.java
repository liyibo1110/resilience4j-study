package com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:42
 */
public class BulkheadEndpointResponse {
    private List<String> bulkheads;

    public BulkheadEndpointResponse() {}

    public BulkheadEndpointResponse(List<String> bulkheads) {
        this.bulkheads = bulkheads;
    }

    public List<String> getBulkheads() {
        return bulkheads;
    }

    public void setBulkheads(List<String> bulkheads) {
        this.bulkheads = bulkheads;
    }
}
