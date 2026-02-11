package com.github.liyibo1110.resilience4j.common.timelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:49
 */
public class TimeLimiterEndpointResponse {
    @Nullable
    private List<String> timeLimiters;

    public TimeLimiterEndpointResponse() {}

    public TimeLimiterEndpointResponse(List<String> timeLimiters){
        this.timeLimiters = timeLimiters;
    }

    @Nullable
    public List<String> getTimeLimiters() {
        return timeLimiters;
    }

    public void setTimeLimiters(List<String> timeLimiters) {
        this.timeLimiters = timeLimiters;
    }
}
