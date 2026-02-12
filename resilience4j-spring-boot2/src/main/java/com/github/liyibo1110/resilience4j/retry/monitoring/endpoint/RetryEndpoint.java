package com.github.liyibo1110.resilience4j.retry.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint.RetryEndpointResponse;
import com.github.liyibo1110.resilience4j.retry.Retry;
import com.github.liyibo1110.resilience4j.retry.RetryRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-11 10:54
 */
@Endpoint(id="retries")
public class RetryEndpoint {
    private final RetryRegistry retryRegistry;

    public RetryEndpoint(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @ReadOperation
    public RetryEndpointResponse getAllRetries() {
        List<String> retries = retryRegistry.getAllRetries().map(Retry::getName).sorted().toJavaList();
        return new RetryEndpointResponse(retries);
    }
}
