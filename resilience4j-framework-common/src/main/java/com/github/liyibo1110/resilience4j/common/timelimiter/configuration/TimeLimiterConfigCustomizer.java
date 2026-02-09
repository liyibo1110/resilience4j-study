package com.github.liyibo1110.resilience4j.common.timelimiter.configuration;

import com.github.liyibo1110.resilience4j.common.CustomizerWithName;
import com.github.liyibo1110.resilience4j.core.lang.NonNull;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterConfig;

import java.util.function.Consumer;

/**
 * @author liyibo
 * @date 2026-02-09 17:50
 */
public interface TimeLimiterConfigCustomizer extends CustomizerWithName {

    void customize(TimeLimiterConfig.Builder configBuilder);

    static TimeLimiterConfigCustomizer of(@NonNull String instanceName,
                                          @NonNull Consumer<TimeLimiterConfig.Builder> consumer) {
        return new TimeLimiterConfigCustomizer() {
            @Override
            public void customize(TimeLimiterConfig.Builder configBuilder) {
                consumer.accept(configBuilder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}
