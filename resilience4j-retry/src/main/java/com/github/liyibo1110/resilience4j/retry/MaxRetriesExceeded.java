package com.github.liyibo1110.resilience4j.retry;

/**
 * retry超时最大尝试次数的对应异常（附带在Event里面，不会真的抛出）
 * @author liyibo
 * @date 2026-02-06 11:55
 */
public class MaxRetriesExceeded extends RuntimeException {
    public MaxRetriesExceeded(String errorMsg) {
        super(errorMsg);
    }
}
