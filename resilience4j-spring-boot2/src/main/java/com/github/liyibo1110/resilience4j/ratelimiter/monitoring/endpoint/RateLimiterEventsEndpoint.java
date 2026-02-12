package com.github.liyibo1110.resilience4j.ratelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.common.ratelimiter.monitoring.endpoint.RateLimiterEventDTO;
import com.github.liyibo1110.resilience4j.common.ratelimiter.monitoring.endpoint.RateLimiterEventsEndpointResponse;
import com.github.liyibo1110.resilience4j.consumer.CircularEventConsumer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.ratelimiter.event.RateLimiterEvent;
import io.vavr.collection.List;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Comparator;

/**
 * @author liyibo
 * @date 2026-02-11 10:53
 */
@Endpoint(id="ratelimiterevents")
public class RateLimiterEventsEndpoint {
    private final EventConsumerRegistry<RateLimiterEvent> eventsConsumerRegistry;

    public RateLimiterEventsEndpoint(EventConsumerRegistry<RateLimiterEvent> eventsConsumerRegistry) {
        this.eventsConsumerRegistry = eventsConsumerRegistry;
    }

    @ReadOperation
    public RateLimiterEventsEndpointResponse getAllRateLimiterEvents() {
        return new RateLimiterEventsEndpointResponse(eventsConsumerRegistry.getAllEventConsumer()
                .flatMap(CircularEventConsumer::getBufferedEvents)
                .sorted(Comparator.comparing(RateLimiterEvent::getCreationTime))
                .map(RateLimiterEventDTO::createRateLimiterEventDTO).toJavaList());
    }

    @ReadOperation
    public RateLimiterEventsEndpointResponse getEventsFilteredByRateLimiterName(@Selector String name) {
        return new RateLimiterEventsEndpointResponse(getRateLimiterEvents(name)
                .map(RateLimiterEventDTO::createRateLimiterEventDTO).toJavaList());
    }

    @ReadOperation
    public RateLimiterEventsEndpointResponse getEventsFilteredByRateLimiterNameAndEventType(@Selector String name,
                                                                                            @Selector String eventType) {
        RateLimiterEvent.Type targetType = RateLimiterEvent.Type.valueOf(eventType.toUpperCase());
        return new RateLimiterEventsEndpointResponse(getRateLimiterEvents(name)
                .filter(event -> event.getEventType() == targetType)
                .map(RateLimiterEventDTO::createRateLimiterEventDTO).toJavaList());
    }

    private List<RateLimiterEvent> getRateLimiterEvents(String name) {
        CircularEventConsumer<RateLimiterEvent> eventConsumer = eventsConsumerRegistry.getEventConsumer(name);
        if(eventConsumer != null) {
            return eventConsumer.getBufferedEvents().filter(event -> event.getRateLimiterName().equals(name));
        }else {
            return List.empty();
        }
    }
}
