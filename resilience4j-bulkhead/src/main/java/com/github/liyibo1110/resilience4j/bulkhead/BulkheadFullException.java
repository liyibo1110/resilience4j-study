package com.github.liyibo1110.resilience4j.bulkhead;

/**
 * 某个bulkhead容量已满对应的异常
 * @author liyibo
 * @date 2026-02-09 10:01
 */
public class BulkheadFullException extends RuntimeException {

    private BulkheadFullException(String message, boolean writableStackTrace) {
        super(message, null, false, writableStackTrace);
    }

    public static BulkheadFullException createBulkheadFullException(Bulkhead bulkhead) {
        boolean writableStackTraceEnabled = bulkhead.getBulkheadConfig().isWritableStackTraceEnabled();
        String message;
        if(Thread.currentThread().isInterrupted())
            message = String.format("Bulkhead '%s' is full and thread was interrupted during permission wait", bulkhead.getName());
        else
            message = String.format("Bulkhead '%s' is full and does not permit further calls", bulkhead.getName());
        return new BulkheadFullException(message, writableStackTraceEnabled);
    }

    public static BulkheadFullException createBulkheadFullException(ThreadPoolBulkhead bulkhead) {
        boolean writableStackTraceEnabled = bulkhead.getBulkheadConfig().isWritableStackTraceEnabled();
        String message = String.format("Bulkhead '%s' is full and does not permit further calls", bulkhead.getName());
        return new BulkheadFullException(message, writableStackTraceEnabled);
    }
}
