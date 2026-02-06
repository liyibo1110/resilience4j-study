package com.github.liyibo1110.resilience4j.ratelimiter.event;

/**
 * @author liyibo
 * @date 2026-02-06 17:28
 */
public class RateLimiterOnDrainedEvent extends AbstractRateLimiterEvent {

    public RateLimiterOnDrainedEvent(String rateLimiterName, int numberOfPermits) {
        super(rateLimiterName, numberOfPermits);
    }

    @Override
    public Type getEventType() {
        return Type.DRAINED;
    }
}
