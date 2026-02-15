package com.github.liyibo1110.resilience4j.circuitbreaker.autoconfigure;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfiguration;
import com.github.liyibo1110.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 这个模块本质上就提供了1个功能：Spring Cloud Config热更新，即增加CircuitBreakerRegistry的特殊功能。
 * 当调用/actuator/refresh接口时，会：
 * 1、重新加载resilience4j配置
 * 2、重新构建Registry
 * 3、重新创建组件实例
 * 核心实现在于@RefreshScope注解
 * @author liyibo
 * @date 2026-02-14 13:13
 */
@Configuration
@ConditionalOnClass({CircuitBreaker.class, RefreshScope.class})
@AutoConfigureAfter(RefreshAutoConfiguration.class)
@AutoConfigureBefore(CircuitBreakerAutoConfiguration.class)
public class RefreshScopedCircuitBreakerAutoConfiguration {
    private final CircuitBreakerConfiguration circuitBreakerConfiguration;

    /** 没用到？？？ */
    private final CircuitBreakerConfigurationProperties circuitBreakerProperties;

    public RefreshScopedCircuitBreakerAutoConfiguration(CircuitBreakerConfigurationProperties circuitBreakerProperties) {
        this.circuitBreakerProperties = circuitBreakerProperties;
        this.circuitBreakerConfiguration = new CircuitBreakerConfiguration(circuitBreakerProperties);
    }

    /**
     * 方法本身的代码，和spring-boot2模块里是完全一致的，但是此模块的方法会尝试优先加载（因为有@AutoConfigureBefore注解）
     */
    @Bean
    @org.springframework.cloud.context.config.annotation.RefreshScope
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
            RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
            @Qualifier("compositeCircuitBreakerCustomizer") CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer) {
        return circuitBreakerConfiguration.circuitBreakerRegistry(eventConsumerRegistry, circuitBreakerRegistryEventConsumer,
                compositeCircuitBreakerCustomizer);
    }
}
