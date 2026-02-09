package com.github.liyibo1110.resilience4j.bulkhead.internal;

import com.github.liyibo1110.resilience4j.core.NamingThreadFactory;

/**
 * 线程名称增加了bulkhead前缀
 * @author liyibo
 * @date 2026-02-09 11:01
 */
class BulkheadNamingThreadFactory extends NamingThreadFactory {
    public BulkheadNamingThreadFactory(String name) {
        super(String.join("-", "bulkhead", name));
    }
}
