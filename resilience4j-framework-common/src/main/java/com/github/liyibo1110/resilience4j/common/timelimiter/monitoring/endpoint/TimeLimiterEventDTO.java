package com.github.liyibo1110.resilience4j.common.timelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:48
 */
public class TimeLimiterEventDTO {
    @Nullable
    private String timeLimiterName;
    @Nullable private TimeLimiterEvent.Type type;
    @Nullable private String creationTime;

    public static TimeLimiterEventDTO createTimeLimiterEventDTO(TimeLimiterEvent timeLimiterEvent) {
        TimeLimiterEventDTO dto = new TimeLimiterEventDTO();
        dto.setTimeLimiterName(timeLimiterEvent.getTimeLimiterName());
        dto.setType(timeLimiterEvent.getEventType());
        dto.setCreationTime(timeLimiterEvent.getCreationTime().toString());
        return dto;
    }

    @Nullable
    public String getTimeLimiterName() {
        return timeLimiterName;
    }

    public void setTimeLimiterName(@Nullable String timeLimiterName) {
        this.timeLimiterName = timeLimiterName;
    }

    @Nullable
    public TimeLimiterEvent.Type getType() {
        return type;
    }

    public void setType(@Nullable TimeLimiterEvent.Type type) {
        this.type = type;
    }

    @Nullable
    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(@Nullable String creationTime) {
        this.creationTime = creationTime;
    }
}
