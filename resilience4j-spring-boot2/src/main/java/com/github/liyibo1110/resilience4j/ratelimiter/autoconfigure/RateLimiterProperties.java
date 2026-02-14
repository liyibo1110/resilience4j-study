package com.github.liyibo1110.resilience4j.ratelimiter.autoconfigure;

import com.github.liyibo1110.resilience4j.ratelimiter.configure.RateLimiterConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liyibo
 * @date 2026-02-13 14:03
 */
@ConfigurationProperties(prefix="resilience4j.ratelimiter")
public class RateLimiterProperties extends RateLimiterConfigurationProperties {

}
