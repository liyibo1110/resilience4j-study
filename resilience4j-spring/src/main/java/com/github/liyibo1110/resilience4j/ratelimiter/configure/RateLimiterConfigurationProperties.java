package com.github.liyibo1110.resilience4j.ratelimiter.configure;

import org.springframework.core.Ordered;

/**
 * @author liyibo
 * @date 2026-02-10 16:03
 */
public class RateLimiterConfigurationProperties extends com.github.liyibo1110.resilience4j.common.ratelimiter.configuration.RateLimiterConfigurationProperties {
    private int rateLimiterAspectOrder = Ordered.LOWEST_PRECEDENCE - 2;

    public int getRateLimiterAspectOrder() {
        return rateLimiterAspectOrder;
    }

    public void setRateLimiterAspectOrder(int rateLimiterAspectOrder) {
        this.rateLimiterAspectOrder = rateLimiterAspectOrder;
    }
}
