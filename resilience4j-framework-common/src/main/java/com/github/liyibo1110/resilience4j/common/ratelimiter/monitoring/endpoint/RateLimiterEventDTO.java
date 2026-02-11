package com.github.liyibo1110.resilience4j.common.ratelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:43
 */
public class RateLimiterEventDTO {
    @Nullable
    private String rateLimiterName;
    @Nullable
    private RateLimiterEvent.Type type;
    @Nullable
    private String creationTime;

    public static RateLimiterEventDTO createRateLimiterEventDTO(RateLimiterEvent rateLimiterEvent) {
        RateLimiterEventDTO dto = new RateLimiterEventDTO();
        dto.setRateLimiterName(rateLimiterEvent.getRateLimiterName());
        dto.setType(rateLimiterEvent.getEventType());
        dto.setCreationTime(rateLimiterEvent.getCreationTime().toString());
        return dto;
    }

    @Nullable
    public String getRateLimiterName() {
        return rateLimiterName;
    }

    public void setRateLimiterName(@Nullable String rateLimiterName) {
        this.rateLimiterName = rateLimiterName;
    }

    @Nullable
    public RateLimiterEvent.Type getType() {
        return type;
    }

    public void setType(@Nullable RateLimiterEvent.Type rateLimiterEventType) {
        this.type = rateLimiterEventType;
    }

    @Nullable
    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(@Nullable String creationTime) {
        this.creationTime = creationTime;
    }
}
