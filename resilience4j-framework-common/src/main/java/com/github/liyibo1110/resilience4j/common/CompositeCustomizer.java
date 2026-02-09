package com.github.liyibo1110.resilience4j.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * name -> CustomizerWithName的映射器
 * @author liyibo
 * @date 2026-02-09 17:22
 */
public class CompositeCustomizer<T extends CustomizerWithName> {

    private final Map<String, T> customizerMap = new HashMap<>();

    public CompositeCustomizer(List<T> customizers) {
        if(customizers != null && !customizers.isEmpty()) {
            customizers.forEach(customizer -> {
                // 不能用重复的key，否则直接抛异常
                if(this.customizerMap.containsKey(customizer.name())) {
                    throw new IllegalStateException("It is not possible to define more than one customizer per instance name "
                                + customizer.name());
                }else {
                    this.customizerMap.put(customizer.name(), customizer);
                }
            });
        }
    }

    public Optional<T> getCustomizer(String instanceName) {
        return Optional.ofNullable(customizerMap.get(instanceName));
    }
}
