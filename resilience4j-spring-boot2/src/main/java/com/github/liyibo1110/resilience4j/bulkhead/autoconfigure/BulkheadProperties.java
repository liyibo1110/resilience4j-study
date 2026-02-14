package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.bulkhead.configure.BulkheadConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liyibo
 * @date 2026-02-13 13:58
 */
@ConfigurationProperties(prefix="resilience4j.bulkhead")
public class BulkheadProperties extends BulkheadConfigurationProperties {

}
