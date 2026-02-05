package com.github.liyibo1110.resilience4j.core;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

/**
 * @author liyibo
 * @date 2026-02-02 17:49
 */
public class StringUtils {
    public static boolean isNotEmpty(@Nullable String str) {
        return str != null && !str.isEmpty();
    }
}
