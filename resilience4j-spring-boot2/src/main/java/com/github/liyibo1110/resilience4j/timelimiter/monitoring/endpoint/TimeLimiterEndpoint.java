package com.github.liyibo1110.resilience4j.timelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.common.timelimiter.monitoring.endpoint.TimeLimiterEndpointResponse;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-11 10:56
 */
@Endpoint(id="timelimiters")
public class TimeLimiterEndpoint {
    private final TimeLimiterRegistry timeLimiterRegistry;

    public TimeLimiterEndpoint(TimeLimiterRegistry timeLimiterRegistry) {
        this.timeLimiterRegistry = timeLimiterRegistry;
    }

    @ReadOperation
    public TimeLimiterEndpointResponse getAllTimeLimiters() {
        List<String> timeLimiters = timeLimiterRegistry.getAllTimeLimiters().map(TimeLimiter::getName).sorted().toJavaList();
        return new TimeLimiterEndpointResponse(timeLimiters);
    }
}
