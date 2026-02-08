package com.github.liyibo1110.resilience4j.timelimiter.event;

/**
 * @author liyibo
 * @date 2026-02-08 18:38
 */
public class TimeLimiterOnErrorEvent extends AbstractTimeLimiterEvent {
    private final Throwable throwable;

    public TimeLimiterOnErrorEvent(String timeLimiterName, Throwable throwable) {
        super(timeLimiterName, Type.ERROR);
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return String.format("%s: TimeLimiter '%s' recorded an error: '%s'",
                getCreationTime(), getTimeLimiterName(), getThrowable());
    }
}
