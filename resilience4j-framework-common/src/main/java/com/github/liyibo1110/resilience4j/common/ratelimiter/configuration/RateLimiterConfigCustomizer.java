package com.github.liyibo1110.resilience4j.common.ratelimiter.configuration;

import com.github.liyibo1110.resilience4j.common.CustomizerWithName;
import com.github.liyibo1110.resilience4j.core.lang.NonNull;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiterConfig;

import java.util.function.Consumer;

/**
 * @author liyibo
 * @date 2026-02-09 17:48
 */
public interface RateLimiterConfigCustomizer extends CustomizerWithName {

    void customize(RateLimiterConfig.Builder configBuilder);

    static RateLimiterConfigCustomizer of(@NonNull String instanceName,
                                          @NonNull Consumer<RateLimiterConfig.Builder> consumer) {
        return new RateLimiterConfigCustomizer() {
            @Override
            public void customize(RateLimiterConfig.Builder configBuilder) {
                consumer.accept(configBuilder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}
