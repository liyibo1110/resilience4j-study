package com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:47
 */
public class RetryEventsEndpointResponse {
    @Nullable
    private List<RetryEventDTO> retryEvents;

    public RetryEventsEndpointResponse() {}

    public RetryEventsEndpointResponse(List<RetryEventDTO> retryEvents) {
        this.retryEvents = retryEvents;
    }

    @Nullable
    public List<RetryEventDTO> getRetryEvents() {
        return retryEvents;
    }

    public void setRetryEvents(List<RetryEventDTO> retryEvents) {
        this.retryEvents = retryEvents;
    }
}
