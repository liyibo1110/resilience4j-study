package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-02-10 14:55
 */
@Configuration
@ConditionalOnClass(CircuitBreaker.class)
public class CircuitBreakerAutoConfiguration {
}
