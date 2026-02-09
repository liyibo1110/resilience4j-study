package com.github.liyibo1110.resilience4j.common.retry.configuration;

import com.github.liyibo1110.resilience4j.common.CustomizerWithName;
import com.github.liyibo1110.resilience4j.core.lang.NonNull;
import com.github.liyibo1110.resilience4j.retry.RetryConfig;

import java.util.function.Consumer;

/**
 * @author liyibo
 * @date 2026-02-09 17:48
 */
public interface RetryConfigCustomizer extends CustomizerWithName {

    void customize(RetryConfig.Builder configBuilder);

    static RetryConfigCustomizer of(@NonNull String instanceName,
                                    @NonNull Consumer<RetryConfig.Builder> consumer) {
        return new RetryConfigCustomizer() {
            @Override
            public void customize(RetryConfig.Builder configBuilder) {
                consumer.accept(configBuilder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}
