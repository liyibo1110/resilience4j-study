package com.github.liyibo1110.resilience4j.ratelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.common.ratelimiter.monitoring.endpoint.RateLimiterEndpointResponse;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-11 10:53
 */
@Endpoint(id="ratelimiters")
public class RateLimiterEndpoint {
    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimiterEndpoint(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @ReadOperation
    public RateLimiterEndpointResponse getAllRateLimiters() {
        List<String> names = rateLimiterRegistry.getAllRateLimiters().map(RateLimiter::getName).sorted().toJavaList();
        return new RateLimiterEndpointResponse(names);
    }
}
