package com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:47
 */
public class RetryEndpointResponse {
    private List<String> retries;

    public RetryEndpointResponse() {
    }

    public RetryEndpointResponse(List<String> retries) {
        this.retries = retries;
    }

    public List<String> getRetries() {
        return retries;
    }

    public void setRetries(List<String> retries) {
        this.retries = retries;
    }
}
