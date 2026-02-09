package com.github.liyibo1110.resilience4j.common;

/**
 * 可定制resilience4j的组件实例名称
 * @author liyibo
 * @date 2026-02-09 17:19
 */
public interface CustomizerWithName {

    /**
     * resilience4j的组件实例名称
     */
    String name();
}
