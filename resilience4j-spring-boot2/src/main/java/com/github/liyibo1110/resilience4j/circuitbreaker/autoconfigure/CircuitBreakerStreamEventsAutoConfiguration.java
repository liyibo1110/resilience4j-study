package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.endpoint.CircuitBreakerHystrixServerSideEvent;
import com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.endpoint.CircuitBreakerServerSideEvent;
import com.github.liyibo1110.resilience4j.reactor.adapter.ReactorAdapter;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

/**
 * @author liyibo
 * @date 2026-02-13 13:56
 */
@Configuration
@ConditionalOnClass({CircuitBreaker.class, Endpoint.class, Flux.class, ReactorAdapter.class})
@AutoConfigureAfter(CircuitBreakerAutoConfiguration.class)
public class CircuitBreakerStreamEventsAutoConfiguration {

    @Bean
    @ConditionalOnAvailableEndpoint
    public CircuitBreakerServerSideEvent circuitBreakerServerSideEventEndpoint(CircuitBreakerRegistry circuitBreakerRegistry) {
        return new CircuitBreakerServerSideEvent(circuitBreakerRegistry);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public CircuitBreakerHystrixServerSideEvent circuitBreakerHystrixServerSideEventEndpoint(CircuitBreakerRegistry circuitBreakerRegistry) {
        return new CircuitBreakerHystrixServerSideEvent(circuitBreakerRegistry);
    }
}
