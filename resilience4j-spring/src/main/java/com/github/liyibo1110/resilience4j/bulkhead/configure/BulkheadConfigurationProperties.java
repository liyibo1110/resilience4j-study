package com.github.liyibo1110.resilience4j.bulkhead.configure;

import org.springframework.core.Ordered;

/**
 * @author liyibo
 * @date 2026-02-10 16:02
 */
public class BulkheadConfigurationProperties extends com.github.liyibo1110.resilience4j.common.bulkhead.configuration.BulkheadConfigurationProperties {
    public int getBulkheadAspectOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
