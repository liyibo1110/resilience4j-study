package com.github.liyibo1110.resilience4j.common.circuitbreaker.configuration;

import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerConfig;
import com.github.liyibo1110.resilience4j.common.CustomizerWithName;
import com.github.liyibo1110.resilience4j.core.lang.NonNull;

import java.util.function.Consumer;

/**
 * 相当于是CircuitBreaker实例的构建工厂，需要传入生成实例的Consumer，之后再调用customize方法传入相应的CircuitBreakerConfig.Builder
 * @author liyibo
 * @date 2026-02-09 17:27
 */
public interface CircuitBreakerConfigCustomizer extends CustomizerWithName {

    void customize(CircuitBreakerConfig.Builder configBuilder);

    static CircuitBreakerConfigCustomizer of(@NonNull String instanceName,
                                             @NonNull Consumer<CircuitBreakerConfig.Builder> consumer) {
        return new CircuitBreakerConfigCustomizer() {
            @Override
            public void customize(CircuitBreakerConfig.Builder configBuilder) {
                consumer.accept(configBuilder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}
