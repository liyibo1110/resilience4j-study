package com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:41
 */
public class BulkheadEventDTO {
    private String bulkheadName;
    private BulkheadEvent.Type type;
    private String creationTime;

    BulkheadEventDTO() {}

    BulkheadEventDTO(String bulkheadName, BulkheadEvent.Type type, String creationTime) {
        this.bulkheadName = bulkheadName;
        this.type = type;
        this.creationTime = creationTime;
    }

    public String getBulkheadName() {
        return bulkheadName;
    }

    public void setBulkheadName(String bulkheadName) {
        this.bulkheadName = bulkheadName;
    }

    public BulkheadEvent.Type getType() {
        return type;
    }

    public void setType(BulkheadEvent.Type type) {
        this.type = type;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
}
