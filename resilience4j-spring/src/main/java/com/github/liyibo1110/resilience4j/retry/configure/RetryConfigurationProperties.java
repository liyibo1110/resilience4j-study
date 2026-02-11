package com.github.liyibo1110.resilience4j.retry.configure;

import org.springframework.core.Ordered;

/**
 * @author liyibo
 * @date 2026-02-10 16:05
 */
public class RetryConfigurationProperties extends com.github.liyibo1110.resilience4j.common.retry.configuration.RetryConfigurationProperties {
    private int retryAspectOrder = Ordered.LOWEST_PRECEDENCE - 4;

    public int getRetryAspectOrder() {
        return retryAspectOrder;
    }

    public void setRetryAspectOrder(int retryAspectOrder) {
        this.retryAspectOrder = retryAspectOrder;
    }
}
