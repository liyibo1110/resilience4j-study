package com.github.liyibo1110.resilience4j.ratelimiter.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-06 17:27
 */
public abstract class AbstractRateLimiterEvent implements RateLimiterEvent {
    private final String rateLimiterName;
    private final int numberOfPermits;
    private final ZonedDateTime creationTime;

    public AbstractRateLimiterEvent(String rateLimiterName) {
        this(rateLimiterName, 1);
    }

    public AbstractRateLimiterEvent(String rateLimiterName, int numberOfPermits) {
        this.rateLimiterName = rateLimiterName;
        this.numberOfPermits = numberOfPermits;
        this.creationTime = ZonedDateTime.now();
    }

    @Override
    public String getRateLimiterName() {
        return rateLimiterName;
    }

    @Override
    public int getNumberOfPermits() {
        return numberOfPermits;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return "RateLimiterEvent{" +
                "type=" + getEventType() +
                ", rateLimiterName='" + getRateLimiterName() + '\'' +
                ", creationTime=" + getCreationTime() +
                '}';
    }
}
