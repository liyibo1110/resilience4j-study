package com.github.liyibo1110.resilience4j.scheduled.threadpool.autoconfigure;

import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-13 14:10
 */
@Configuration
@ConditionalOnProperty(value="resilience4j.scheduled.executor.corePoolSize")
@EnableConfigurationProperties({ContextAwareScheduledThreadPoolProperties.class})
public class ContextAwareScheduledThreadPoolAutoConfiguration {
    @Bean
    public ContextAwareScheduledThreadPoolExecutor getContextAwareScheduledThreadPool(ContextAwareScheduledThreadPoolProperties prop) {
        return prop.build();
    }
}
