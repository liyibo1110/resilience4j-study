package com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.endpoint;

import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint.CircuitBreakerEventDTOFactory;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint.CircuitBreakerEventsEndpointResponse;
import com.github.liyibo1110.resilience4j.consumer.CircularEventConsumer;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import io.vavr.collection.List;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Comparator;

/**
 * @author liyibo
 * @date 2026-02-10 17:00
 */
@Endpoint(id="circuitbreakerevents")
public class CircuitBreakerEventsEndpoint {
    private final EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry;

    public CircuitBreakerEventsEndpoint(EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry) {
        this.eventConsumerRegistry = eventConsumerRegistry;
    }

    @ReadOperation
    public CircuitBreakerEventsEndpointResponse getAllCircuitBreakerEvents() {
        return new CircuitBreakerEventsEndpointResponse(eventConsumerRegistry.getAllEventConsumer()
                .flatMap(CircularEventConsumer::getBufferedEvents)
                .sorted(Comparator.comparing(CircuitBreakerEvent::getCreationTime))
                .map(CircuitBreakerEventDTOFactory::createCircuitBreakerEventDTO).toJavaList());
    }

    /**
     * 根据name过滤
     */
    @ReadOperation
    public CircuitBreakerEventsEndpointResponse getEventsFilteredByCircuitBreakerName(@Selector String name) {
        return new CircuitBreakerEventsEndpointResponse(getCircuitBreakerEvents(name)
                .map(CircuitBreakerEventDTOFactory::createCircuitBreakerEventDTO).toJavaList());
    }

    /**
     * 根据name和eventType过滤
     */
    @ReadOperation
    public CircuitBreakerEventsEndpointResponse getEventsFilteredByCircuitBreakerNameAndEventType(@Selector String name,
                                                                                                  @Selector String eventType) {
        return new CircuitBreakerEventsEndpointResponse(getCircuitBreakerEvents(name)
                .filter(event -> event.getEventType() == CircuitBreakerEvent.Type.valueOf(eventType.toUpperCase()))
                .map(CircuitBreakerEventDTOFactory::createCircuitBreakerEventDTO).toJavaList());
    }

    private List<CircuitBreakerEvent> getCircuitBreakerEvents(String circuitBreakerName) {
        CircularEventConsumer<CircuitBreakerEvent> eventConsumer = eventConsumerRegistry.getEventConsumer(circuitBreakerName);
        if(eventConsumer != null)
            return eventConsumer.getBufferedEvents().filter(event -> event.getCircuitBreakerName().equals(circuitBreakerName));
        else
            return List.empty();
    }
}
