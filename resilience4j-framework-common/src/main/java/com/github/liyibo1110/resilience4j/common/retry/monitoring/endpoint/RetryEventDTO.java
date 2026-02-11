package com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:45
 */
public class RetryEventDTO {
    private String retryName;
    private RetryEvent.Type type;
    private String creationTime;
    private String errorMessage;
    private int numberOfAttempts;

    RetryEventDTO() {}

    RetryEventDTO(String retryName, RetryEvent.Type type,
                  String creationTime, String errorMessage,
                  int numberOfAttempts) {
        this.retryName = retryName;
        this.type = type;
        this.creationTime = creationTime;
        this.errorMessage = errorMessage;
        this.numberOfAttempts = numberOfAttempts;
    }

    public String getRetryName() {
        return retryName;
    }

    public void setRetryName(String retryName) {
        this.retryName = retryName;
    }

    public RetryEvent.Type getType() {
        return type;
    }

    public void setType(RetryEvent.Type type) {
        this.type = type;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts(int numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }
}
