package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用来存项目配置文件中circuitBreaker配置字段
 * @author liyibo
 * @date 2026-02-13 12:03
 */
@ConfigurationProperties(prefix="resilience4j.circuitbreaker")
public class CircuitBreakerProperties extends CircuitBreakerConfigurationProperties {

}
