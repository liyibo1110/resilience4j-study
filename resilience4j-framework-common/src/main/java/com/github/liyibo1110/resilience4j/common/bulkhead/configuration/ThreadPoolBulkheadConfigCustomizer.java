package com.github.liyibo1110.resilience4j.common.bulkhead.configuration;

import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import com.github.liyibo1110.resilience4j.common.CustomizerWithName;
import com.github.liyibo1110.resilience4j.core.lang.NonNull;

import java.util.function.Consumer;

/**
 * @author liyibo
 * @date 2026-02-09 17:50
 */
public interface ThreadPoolBulkheadConfigCustomizer extends CustomizerWithName {

    void customize(ThreadPoolBulkheadConfig.Builder configBuilder);

    static ThreadPoolBulkheadConfigCustomizer of(@NonNull String instanceName,
                                                 @NonNull Consumer<ThreadPoolBulkheadConfig.Builder> consumer) {
        return new ThreadPoolBulkheadConfigCustomizer() {

            @Override
            public void customize(ThreadPoolBulkheadConfig.Builder configBuilder) {
                consumer.accept(configBuilder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}
