package com.github.liyibo1110.resilience4j.retry.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint.RetryEventDTOFactory;
import com.github.liyibo1110.resilience4j.common.retry.monitoring.endpoint.RetryEventsEndpointResponse;
import com.github.liyibo1110.resilience4j.consumer.CircularEventConsumer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.retry.event.RetryEvent;
import io.vavr.collection.List;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Comparator;

/**
 * @author liyibo
 * @date 2026-02-11 10:55
 */
@Endpoint(id="retryevents")
public class RetryEventsEndpoint {
    private final EventConsumerRegistry<RetryEvent> eventConsumerRegistry;

    public RetryEventsEndpoint(EventConsumerRegistry<RetryEvent> eventConsumerRegistry) {
        this.eventConsumerRegistry = eventConsumerRegistry;
    }

    @ReadOperation
    public RetryEventsEndpointResponse getAllRetryEvents() {
        return new RetryEventsEndpointResponse(eventConsumerRegistry.getAllEventConsumer()
                .flatMap(CircularEventConsumer::getBufferedEvents)
                .sorted(Comparator.comparing(RetryEvent::getCreationTime))
                .map(RetryEventDTOFactory::createRetryEventDTO).toJavaList());
    }

    @ReadOperation
    public RetryEventsEndpointResponse getEventsFilteredByRetryName(@Selector String name) {
        return new RetryEventsEndpointResponse(getRetryEvents(name).map(RetryEventDTOFactory::createRetryEventDTO).toJavaList());
    }

    @ReadOperation
    public RetryEventsEndpointResponse getEventsFilteredByRetryNameAndEventType(@Selector String name,
                                                                                @Selector String eventType) {
        return new RetryEventsEndpointResponse(getRetryEvents(name)
                .filter(event -> event.getEventType() == RetryEvent.Type.valueOf(eventType.toUpperCase()))
                .map(RetryEventDTOFactory::createRetryEventDTO).toJavaList());
    }

    private List<RetryEvent> getRetryEvents(String name) {
        final CircularEventConsumer<RetryEvent> syncEvents = eventConsumerRegistry.getEventConsumer(name);
        if(syncEvents != null)
            return syncEvents.getBufferedEvents().filter(event -> event.getName().equals(name));
        else
            return List.empty();
    }
}
