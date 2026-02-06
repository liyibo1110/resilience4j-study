package com.github.liyibo1110.resilience4j.core.exception;

/**
 * 获取许可过程中线程被中断，所对应的异常（在多个模块中被共用）
 * @author liyibo
 * @date 2026-02-06 23:25
 */
public class AcquirePermissionCancelledException extends IllegalStateException {
    private static final String DEFAULT_MESSAGE = "Thread was interrupted while waiting for a permission";

    public AcquirePermissionCancelledException() {
        super(DEFAULT_MESSAGE);
    }

    public AcquirePermissionCancelledException(String message) {
        super(message);
    }
}
