package com.github.liyibo1110.resilience4j.scheduled.threadpool.autoconfigure;

import com.github.liyibo1110.resilience4j.common.scheduled.threadpool.configuration.ContextAwareScheduledThreadPoolConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liyibo
 * @date 2026-02-13 14:10
 */
@ConfigurationProperties(prefix = "resilience4j.scheduled.executor")
public class ContextAwareScheduledThreadPoolProperties extends ContextAwareScheduledThreadPoolConfigurationProperties {

}
