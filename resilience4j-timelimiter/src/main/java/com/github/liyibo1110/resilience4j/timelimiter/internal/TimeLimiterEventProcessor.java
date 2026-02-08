package com.github.liyibo1110.resilience4j.timelimiter.internal;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.core.EventProcessor;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnErrorEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnSuccessEvent;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterOnTimeoutEvent;

/**
 * @author liyibo
 * @date 2026-02-08 18:41
 */
public class TimeLimiterEventProcessor extends EventProcessor<TimeLimiterEvent>
        implements EventConsumer<TimeLimiterEvent>, TimeLimiter.EventPublisher {

    @Override
    public void consumeEvent(TimeLimiterEvent event) {
        super.processEvent(event);
    }

    @Override
    public TimeLimiter.EventPublisher onSuccess(EventConsumer<TimeLimiterOnSuccessEvent> eventConsumer) {
        registerConsumer(TimeLimiterOnSuccessEvent.class.getName(), eventConsumer);
        return this;
    }

    @Override
    public TimeLimiter.EventPublisher onError(EventConsumer<TimeLimiterOnErrorEvent> eventConsumer) {
        registerConsumer(TimeLimiterOnErrorEvent.class.getName(), eventConsumer);
        return this;
    }

    @Override
    public TimeLimiter.EventPublisher onTimeout(EventConsumer<TimeLimiterOnTimeoutEvent> eventConsumer) {
        registerConsumer(TimeLimiterOnTimeoutEvent.class.getName(), eventConsumer);
        return this;
    }
}
