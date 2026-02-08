package com.github.liyibo1110.resilience4j.timelimiter.event;

/**
 * @author liyibo
 * @date 2026-02-08 18:38
 */
public class TimeLimiterOnSuccessEvent extends AbstractTimeLimiterEvent {
    public TimeLimiterOnSuccessEvent(String timeLimiterName) {
        super(timeLimiterName, Type.SUCCESS);
    }

    @Override
    public String toString() {
        return String.format("%s: TimeLimiter '%s' recorded a successful call.",
                getCreationTime(), getTimeLimiterName());
    }
}
