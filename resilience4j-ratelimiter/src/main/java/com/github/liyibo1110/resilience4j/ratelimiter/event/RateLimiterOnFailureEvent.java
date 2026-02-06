package com.github.liyibo1110.resilience4j.ratelimiter.event;

/**
 * @author liyibo
 * @date 2026-02-06 17:29
 */
public class RateLimiterOnFailureEvent extends AbstractRateLimiterEvent {

    public RateLimiterOnFailureEvent(String rateLimiterName) {
        super(rateLimiterName, 1);
    }

    public RateLimiterOnFailureEvent(String rateLimiterName, int numberOfPermits) {
        super(rateLimiterName, numberOfPermits);
    }

    @Override
    public Type getEventType() {
        return Type.FAILED_ACQUIRE;
    }
}
