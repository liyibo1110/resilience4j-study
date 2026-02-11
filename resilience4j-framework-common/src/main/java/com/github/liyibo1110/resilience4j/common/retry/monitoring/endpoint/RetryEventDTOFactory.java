package com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnErrorEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnIgnoredErrorEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnRetryEvent;
import com.github.liyibo1110.resilience4j.retry.event.RetryOnSuccessEvent;

/**
 * @author liyibo
 * @date 2026-02-10 00:46
 */
public class RetryEventDTOFactory {
    private RetryEventDTOFactory() {}

    public static RetryEventDTO createRetryEventDTO(RetryEvent event) {
        switch(event.getEventType()) {
            case ERROR:
                RetryOnErrorEvent onErrorEvent = (RetryOnErrorEvent)event;
                return newRetryEventDTOBuilder(onErrorEvent)
                        .throwable(onErrorEvent.getLastThrowable())
                        .numberOfAttempts(onErrorEvent.getNumberOfRetryAttempts())
                        .build();
            case SUCCESS:
                RetryOnSuccessEvent onSuccessEvent = (RetryOnSuccessEvent)event;
                return newRetryEventDTOBuilder(onSuccessEvent)
                        .numberOfAttempts(onSuccessEvent.getNumberOfRetryAttempts())
                        .throwable(onSuccessEvent.getLastThrowable())
                        .build();
            case RETRY:
                RetryOnRetryEvent onStateTransitionEvent = (RetryOnRetryEvent)event;
                return newRetryEventDTOBuilder(onStateTransitionEvent)
                        .throwable(onStateTransitionEvent.getLastThrowable())
                        .numberOfAttempts(onStateTransitionEvent.getNumberOfRetryAttempts())
                        .build();
            case IGNORED_ERROR:
                RetryOnIgnoredErrorEvent onIgnoredErrorEvent = (RetryOnIgnoredErrorEvent)event;
                return newRetryEventDTOBuilder(onIgnoredErrorEvent)
                        .throwable(onIgnoredErrorEvent.getLastThrowable())
                        .build();
            default:
                throw new IllegalArgumentException("Invalid event");
        }
    }

    private static RetryEventDTOBuilder newRetryEventDTOBuilder(RetryEvent event) {
        return new RetryEventDTOBuilder(event.getName(), event.getEventType(), event.getCreationTime().toString());
    }
}
