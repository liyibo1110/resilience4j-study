package com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:46
 */
class RetryEventDTOBuilder {
    private final String retryName;
    private final RetryEvent.Type type;
    private final String creationTime;
    private String errorMessage;
    private int numberOfAttempts;

    RetryEventDTOBuilder(String retryName, RetryEvent.Type type, String creationTime) {
        this.retryName = retryName;
        this.type = type;
        this.creationTime = creationTime;
    }

    RetryEventDTOBuilder throwable(@Nullable Throwable throwable) {
        if(throwable != null)
            this.errorMessage = throwable.toString();
        return this;
    }

    RetryEventDTOBuilder numberOfAttempts(int numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
        return this;
    }


    RetryEventDTO build() {
        return new RetryEventDTO(retryName, type, creationTime, errorMessage, numberOfAttempts);
    }
}
