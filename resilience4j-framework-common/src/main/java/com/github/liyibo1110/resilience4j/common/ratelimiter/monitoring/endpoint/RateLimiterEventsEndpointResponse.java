package com.github.liyibo1110.resilience4j.common.ratelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:44
 */
public class RateLimiterEventsEndpointResponse {
    @Nullable
    private List<RateLimiterEventDTO> rateLimiterEvents;

    public RateLimiterEventsEndpointResponse() {}

    public RateLimiterEventsEndpointResponse(@Nullable List<RateLimiterEventDTO> rateLimiterEvents) {
        this.rateLimiterEvents = rateLimiterEvents;
    }

    @Nullable
    public List<RateLimiterEventDTO> getRateLimiterEvents() {
        return rateLimiterEvents;
    }

    public void setRateLimiterEvents(@Nullable List<RateLimiterEventDTO> rateLimiterEvents) {
        this.rateLimiterEvents = rateLimiterEvents;
    }
}
