package com.github.liyibo1110.resilience4j.bulkhead.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint.BulkheadEndpointResponse;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-11 10:43
 */
@Endpoint(id="bulkheads")
public class BulkheadEndpoint {
    private final BulkheadRegistry bulkheadRegistry;
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;

    public BulkheadEndpoint(BulkheadRegistry bulkheadRegistry, ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry) {
        this.bulkheadRegistry = bulkheadRegistry;
        this.threadPoolBulkheadRegistry = threadPoolBulkheadRegistry;
    }

    @ReadOperation
    public BulkheadEndpointResponse getAllBulkheads() {
        List<String> bulkheads = bulkheadRegistry.getAllBulkheads()
                .map(Bulkhead::getName)
                .appendAll(threadPoolBulkheadRegistry
                        .getAllBulkheads()
                        .map(ThreadPoolBulkhead::getName)).sorted().toJavaList();
        return new BulkheadEndpointResponse(bulkheads);
    }
}
