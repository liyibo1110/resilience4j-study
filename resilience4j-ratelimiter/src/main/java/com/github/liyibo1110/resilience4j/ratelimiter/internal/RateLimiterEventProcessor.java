package com.github.liyibo1110.resilience4j.ratelimiter.internal;

import com.github.liyibo1110.resilience4j.core.EventConsumer;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnFailureEvent;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterOnSuccessEvent;

/**
 * @author liyibo
 * @date 2026-02-06 23:29
 */
public class RateLimiterEventProcessor extends com.github.liyibo1110.resilience4j.core.EventProcessor<RateLimiterEvent>
            implements EventConsumer<RateLimiterEvent>, RateLimiter.EventPublisher {
    @Override
    public void consumeEvent(RateLimiterEvent event) {
        super.processEvent(event);
    }

    @Override
    public RateLimiter.EventPublisher onSuccess(EventConsumer<RateLimiterOnSuccessEvent> eventConsumer) {
        registerConsumer(RateLimiterOnSuccessEvent.class.getName(), eventConsumer);
        return this;
    }

    @Override
    public RateLimiter.EventPublisher onFailure(EventConsumer<RateLimiterOnFailureEvent> eventConsumer) {
        registerConsumer(RateLimiterOnFailureEvent.class.getName(), eventConsumer);
        return this;
    }
}
