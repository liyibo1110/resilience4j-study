package com.github.liyibo1110.resilience4j.timelimiter.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.common.timelimiter.monitoring.endpoint.TimeLimiterEventDTO;
import com.github.liyibo1110.resilience4j.common.timelimiter.monitoring.endpoint.TimeLimiterEventsEndpointResponse;
import com.github.liyibo1110.resilience4j.consumer.CircularEventConsumer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import io.vavr.collection.List;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Comparator;

/**
 * @author liyibo
 * @date 2026-02-11 10:57
 */
@Endpoint(id="timelimiterevents")
public class TimeLimiterEventsEndpoint {
    private final EventConsumerRegistry<TimeLimiterEvent> eventsConsumerRegistry;

    public TimeLimiterEventsEndpoint(EventConsumerRegistry<TimeLimiterEvent> eventsConsumerRegistry) {
        this.eventsConsumerRegistry = eventsConsumerRegistry;
    }

    @ReadOperation
    public TimeLimiterEventsEndpointResponse getAllTimeLimiterEvents() {
        return new TimeLimiterEventsEndpointResponse(eventsConsumerRegistry.getAllEventConsumer()
                .flatMap(CircularEventConsumer::getBufferedEvents)
                .sorted(Comparator.comparing(TimeLimiterEvent::getCreationTime))
                .map(TimeLimiterEventDTO::createTimeLimiterEventDTO).toJavaList());
    }

    @ReadOperation
    public TimeLimiterEventsEndpointResponse getEventsFilteredByTimeLimiterName(@Selector String name) {
        return new TimeLimiterEventsEndpointResponse(getTimeLimiterEvents(name)
                .map(TimeLimiterEventDTO::createTimeLimiterEventDTO).toJavaList());
    }

    @ReadOperation
    public TimeLimiterEventsEndpointResponse getEventsFilteredByTimeLimiterNameAndEventType(@Selector String name,
                                                                                            @Selector String eventType) {
        TimeLimiterEvent.Type targetType = TimeLimiterEvent.Type.valueOf(eventType.toUpperCase());
        return new TimeLimiterEventsEndpointResponse(getTimeLimiterEvents(name)
                .filter(event -> event.getEventType() == targetType)
                .map(TimeLimiterEventDTO::createTimeLimiterEventDTO).toJavaList());
    }

    private List<TimeLimiterEvent> getTimeLimiterEvents(String name) {
        CircularEventConsumer<TimeLimiterEvent> eventConsumer = eventsConsumerRegistry.getEventConsumer(name);
        if(eventConsumer != null)
            return eventConsumer.getBufferedEvents().filter(event -> event.getTimeLimiterName().equals(name));
        else
            return List.empty();
    }
}
