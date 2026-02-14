package com.github.liyibo1110.resilience4j.timelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liyibo
 * @date 2026-02-13 14:11
 */
@ConfigurationProperties(prefix="resilience4j.timelimiter")
public class TimeLimiterProperties extends TimeLimiterConfigurationProperties {

}
