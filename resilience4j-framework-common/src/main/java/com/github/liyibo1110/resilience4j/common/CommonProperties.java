package com.github.liyibo1110.resilience4j.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用的公共属性表
 * @author liyibo
 * @date 2026-02-09 17:18
 */
public class CommonProperties {
    Map<String, String> tags = new HashMap<>();

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
