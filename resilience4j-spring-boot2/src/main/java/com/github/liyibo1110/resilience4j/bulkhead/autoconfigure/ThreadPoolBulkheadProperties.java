package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liyibo
 * @date 2026-02-13 13:59
 */
@ConfigurationProperties(prefix = "resilience4j.thread-pool-bulkhead")
public class ThreadPoolBulkheadProperties extends ThreadPoolBulkheadConfigurationProperties {

}
