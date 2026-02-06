package com.github.liyibo1110.resilience4j.ratelimiter.event;

import java.time.ZonedDateTime;

/**
 * @author liyibo
 * @date 2026-02-06 17:26
 */
public interface RateLimiterEvent {
    String getRateLimiterName();
    Type getEventType();
    int getNumberOfPermits();
    ZonedDateTime getCreationTime();

    enum Type {
        FAILED_ACQUIRE, // 获取许可失败
        SUCCESSFUL_ACQUIRE, // 获取许可成功
        DRAINED
    }
}
