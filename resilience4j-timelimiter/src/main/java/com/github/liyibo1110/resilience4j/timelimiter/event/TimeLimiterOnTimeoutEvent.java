package com.github.liyibo1110.resilience4j.timelimiter.event;

/**
 * @author liyibo
 * @date 2026-02-08 18:38
 */
public class TimeLimiterOnTimeoutEvent extends AbstractTimeLimiterEvent {
    public TimeLimiterOnTimeoutEvent(String timeLimiterName) {
        super(timeLimiterName, Type.TIMEOUT);
    }

    @Override
    public String toString() {
        return String.format("%s: TimeLimiter '%s' recorded a timeout exception.",
                getCreationTime(), getTimeLimiterName());
    }
}
