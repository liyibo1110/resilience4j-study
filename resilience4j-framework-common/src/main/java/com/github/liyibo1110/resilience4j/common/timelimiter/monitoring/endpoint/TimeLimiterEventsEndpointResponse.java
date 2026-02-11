package com.github.liyibo1110.resilience4j.common.timelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-10 00:49
 */
public class TimeLimiterEventsEndpointResponse {
    @Nullable
    private List<TimeLimiterEventDTO> timeLimiterEvents;

    public TimeLimiterEventsEndpointResponse() {}

    public TimeLimiterEventsEndpointResponse(@Nullable List<TimeLimiterEventDTO> timeLimiterEvents) {
        this.timeLimiterEvents = timeLimiterEvents;
    }

    @Nullable
    public List<TimeLimiterEventDTO> getTimeLimiterEvents() {
        return timeLimiterEvents;
    }

    public void setTimeLimiterEvents(@Nullable List<TimeLimiterEventDTO> timeLimiterEvents) {
        this.timeLimiterEvents = timeLimiterEvents;
    }
}
