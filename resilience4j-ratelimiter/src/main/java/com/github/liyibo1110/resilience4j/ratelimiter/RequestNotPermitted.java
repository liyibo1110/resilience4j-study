package com.github.liyibo1110.resilience4j.ratelimiter;

/**
 * 未能获得许可的对应异常（附带在Event里面，不会真的抛出）
 * @author liyibo
 * @date 2026-02-06 17:31
 */
public class RequestNotPermitted extends RuntimeException {

    private RequestNotPermitted(String message, boolean writableStackTrace) {
        super(message, null, false, writableStackTrace);
    }

    public static RequestNotPermitted createRequestNotPermitted(RateLimiter rateLimiter) {
        boolean writableStackTraceEnabled = rateLimiter.getRateLimiterConfig().isWritableStackTraceEnabled();
        String message = String.format("RateLimiter '%s' does not permit further calls", rateLimiter.getName());
        return new RequestNotPermitted(message, writableStackTraceEnabled);
    }
}
