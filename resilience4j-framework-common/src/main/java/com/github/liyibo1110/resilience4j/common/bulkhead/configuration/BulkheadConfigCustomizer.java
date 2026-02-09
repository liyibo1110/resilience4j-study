package com.github.liyibo1110.resilience4j.common.bulkhead.configuration;

import com.github.liyibo1110.resilience4j.bulkhead.BulkheadConfig;
import com.github.liyibo1110.resilience4j.common.CustomizerWithName;
import com.github.liyibo1110.resilience4j.core.lang.NonNull;

import java.util.function.Consumer;

/**
 * @author liyibo
 * @date 2026-02-09 17:47
 */
public interface BulkheadConfigCustomizer extends CustomizerWithName {

    void customize(BulkheadConfig.Builder configBuilder);

    static  BulkheadConfigCustomizer of(@NonNull String instanceName,
                                        @NonNull Consumer<BulkheadConfig.Builder> consumer) {
        return new BulkheadConfigCustomizer() {
            @Override
            public void customize(BulkheadConfig.Builder configBuilder) {
                consumer.accept(configBuilder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}
