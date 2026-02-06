package com.github.liyibo1110.resilience4j.retry;

/**
 * retry超时最大尝试次数的对应异常
 * @author liyibo
 * @date 2026-02-06 11:55
 */
public class MaxRetriesExceededException extends RuntimeException {
    private final transient String causingRetryName;

    private MaxRetriesExceededException(String causingRetryName, String message, boolean writeableStackTrace) {
        super(message, null, false, writeableStackTrace);
        this.causingRetryName = causingRetryName;
    }

    /**
     * 构造工厂
     */
    public static MaxRetriesExceededException createMaxRetriesExceededException(Retry retry) {
        boolean writeStackTrace = retry.getRetryConfig().isWritableStackTraceEnabled();
        String message = String.format("Retry '%s' has exhausted all attempts (%d)",
                retry.getName(), retry.getRetryConfig().getMaxAttempts());
        return new MaxRetriesExceededException(retry.getName(), message, writeStackTrace);
    }

    public String getCausingRetryName() {
        return causingRetryName;
    }
}
