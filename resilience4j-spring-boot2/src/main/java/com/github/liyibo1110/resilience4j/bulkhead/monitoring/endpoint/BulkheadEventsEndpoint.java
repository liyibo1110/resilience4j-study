package com.github.liyibo1110.resilience4j.bulkhead.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint.BulkheadEventDTO;
import com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint.BulkheadEventDTOFactory;
import com.github.liyibo1110.resilience4j.common.bulkhead.monitoring.endpoint.BulkheadEventsEndpointResponse;
import com.github.liyibo1110.resilience4j.consumer.CircularEventConsumer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import io.vavr.collection.List;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Comparator;

/**
 * @author liyibo
 * @date 2026-02-11 10:51
 */
@Endpoint(id="bulkheadevents")
public class BulkheadEventsEndpoint {
    private final EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry;

    public BulkheadEventsEndpoint(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry) {
        this.eventConsumerRegistry = eventConsumerRegistry;
    }

    @ReadOperation
    public BulkheadEventsEndpointResponse getAllBulkheadEvents() {
        java.util.List<BulkheadEventDTO> response = eventConsumerRegistry.getAllEventConsumer()
                .flatMap(CircularEventConsumer::getBufferedEvents)
                .sorted(Comparator.comparing(BulkheadEvent::getCreationTime))
                .map(BulkheadEventDTOFactory::createBulkheadEventDTO)
                .toJavaList();
        return new BulkheadEventsEndpointResponse(response);
    }

    @ReadOperation
    public BulkheadEventsEndpointResponse getEventsFilteredByBulkheadNameAndEventType(
            @Selector String bulkheadName, @Selector String eventType) {
        java.util.List<BulkheadEventDTO> response = getBulkheadEvent(bulkheadName)
                .filter(event -> event.getEventType() == BulkheadEvent.Type.valueOf(eventType.toUpperCase()))
                .map(BulkheadEventDTOFactory::createBulkheadEventDTO)
                .toJavaList();
        return new BulkheadEventsEndpointResponse(response);
    }

    private List<BulkheadEvent> getBulkheadEvent(String bulkheadName) {
        CircularEventConsumer<BulkheadEvent> eventConsumer = eventConsumerRegistry.getEventConsumer(bulkheadName);
        if(eventConsumer == null) {
            CircularEventConsumer<BulkheadEvent> threadPoolEventConsumer = eventConsumerRegistry
                    .getEventConsumer(String.join("-", ThreadPoolBulkhead.class.getSimpleName(), bulkheadName));
            if(threadPoolEventConsumer != null)
                return threadPoolEventConsumer.getBufferedEvents().filter(event -> event.getBulkheadName().equals(bulkheadName));
            else
                return List.empty();
        } else {
            return eventConsumer.getBufferedEvents().filter(event -> event.getBulkheadName().equals(bulkheadName));
        }
    }
}
