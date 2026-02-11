package com.github.liyibo1110.resilience4j.timelimiter.configure;

import org.springframework.core.Ordered;

/**
 * @author liyibo
 * @date 2026-02-10 16:05
 */
public class TimeLimiterConfigurationProperties extends com.github.liyibo1110.resilience4j.common.timelimiter.configuration.TimeLimiterConfigurationProperties {
    private int timeLimiterAspectOrder = Ordered.LOWEST_PRECEDENCE - 1;

    public int getTimeLimiterAspectOrder() {
        return timeLimiterAspectOrder;
    }

    public void setTimeLimiterAspectOrder(int timeLimiterAspectOrder) {
        this.timeLimiterAspectOrder = timeLimiterAspectOrder;
    }
}
