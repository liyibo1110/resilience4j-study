package com.github.liyibo1110.resilience4j.circuitbreaker.configure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * @author liyibo
 * @date 2026-02-11 15:32
 */
@Aspect
public class CircuitBreakerAspect implements Ordered {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerAspect.class);
    private final CircuitBreakerConfigurationProperties prop;
    private final CircuitBreakerRegistry circuitBreakerRegistry;


    @Override
    public int getOrder() {
        return 0;
    }
}
