package com.github.liyibo1110.resilience4j.ratelimiter.event;

/**
 * @author liyibo
 * @date 2026-02-06 17:28
 */
public class RateLimiterOnSuccessEvent extends AbstractRateLimiterEvent {

    public RateLimiterOnSuccessEvent(String rateLimiterName) {
        super(rateLimiterName, 1);
    }

    public RateLimiterOnSuccessEvent(String rateLimiterName, int numberOfPermits) {
        super(rateLimiterName, numberOfPermits);
    }

    @Override
    public Type getEventType() {
        return Type.SUCCESSFUL_ACQUIRE;
    }
}
