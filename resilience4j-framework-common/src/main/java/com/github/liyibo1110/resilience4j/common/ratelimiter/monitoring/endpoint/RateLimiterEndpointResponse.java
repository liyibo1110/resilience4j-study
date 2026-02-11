package com.github.liyibo1110.resilience4j.common.ratelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:44
 */
public class RateLimiterEndpointResponse {
    @Nullable
    private List<String> rateLimiters;

    // created for spring to be able to construct POJO
    public RateLimiterEndpointResponse() {}

    public RateLimiterEndpointResponse(@Nullable List<String> rateLimiters) {
        this.rateLimiters = rateLimiters;
    }

    @Nullable
    public List<String> getRateLimiters() {
        return rateLimiters;
    }

    public void setRateLimiters(@Nullable List<String> rateLimiters) {
        this.rateLimiters = rateLimiters;
    }
}
