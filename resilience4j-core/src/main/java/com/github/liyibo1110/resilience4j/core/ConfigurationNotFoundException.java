package com.github.liyibo1110.resilience4j.core;

/**
 * 根据配置name，找不到对应配置时的异常。
 * @author liyibo
 * @date 2026-02-02 18:12
 */
public class ConfigurationNotFoundException extends RuntimeException {
    public ConfigurationNotFoundException(String configName) {
        super(String.format("Configuration with name '%s' does not exist", configName));
    }
}
