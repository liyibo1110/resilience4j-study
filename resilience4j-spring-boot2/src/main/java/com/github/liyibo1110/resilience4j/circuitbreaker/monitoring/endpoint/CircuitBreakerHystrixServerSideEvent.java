package com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint.CircuitBreakerHystrixStreamEventsDTO;
import com.github.liyibo1110.resilience4j.reactor.adapter.ReactorAdapter;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.BiFunction;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * @author liyibo
 * @date 2026-02-11 10:35
 */
@Endpoint(id="hystrixstreamcircuitbreakerevents")
public class CircuitBreakerHystrixServerSideEvent {
    private final CircuitBreakerRegistry cbr;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public CircuitBreakerHystrixServerSideEvent(CircuitBreakerRegistry cbr) {
        this.cbr = cbr;
    }

    @ReadOperation(produces=TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getAllCircuitBreakerHystrixStreamEvents() {
        Seq<Flux<CircuitBreakerEvent>> eventStreams = cbr.getAllCircuitBreakers()
                .map(circuitBreaker -> ReactorAdapter.toFlux(circuitBreaker.getEventPublisher()));
        // 没用到这个data？？？
        BiFunction<CircuitBreakerEvent, CircuitBreaker, String> data = getCircuitBreakerEventStringFunction();
        return Flux.merge(publishEvents(Array.ofAll(eventStreams)), getHeartbeatStream());
    }

    @ReadOperation(produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getHystrixStreamEventsFilteredByCircuitBreakerName(
            @Selector String name) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(name);
        Flux<CircuitBreakerEvent> eventStream = ReactorAdapter.toFlux(circuitBreaker.getEventPublisher());
        return Flux.merge(publishEvents(Array.of(eventStream)), getHeartbeatStream());

    }

    @ReadOperation(produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getHystrixStreamEventsFilteredByCircuitBreakerNameAndEventType(
            @Selector String name, @Selector String eventType) {
        CircuitBreaker givenCircuitBreaker = getCircuitBreaker(name);
        Flux<CircuitBreakerEvent> eventStream = ReactorAdapter.toFlux(givenCircuitBreaker.getEventPublisher())
                .filter(event -> event.getEventType() == CircuitBreakerEvent.Type.valueOf(eventType.toUpperCase()));
        return Flux.merge(publishEvents(Array.of(eventStream)), getHeartbeatStream());

    }

    private Flux<ServerSentEvent<String>> publishEvents(Seq<Flux<CircuitBreakerEvent>> eventStreams) {
        BiFunction<CircuitBreakerEvent, CircuitBreaker, String> circuitBreakerEventDataFn = getCircuitBreakerEventStringFunction();
        return Flux.merge(eventStreams)
                .onBackpressureDrop()
                .delayElements(Duration.ofMillis(100))
                .map(cbEvent ->
                        ServerSentEvent.<String>builder()
                                .id(cbEvent.getCircuitBreakerName())
                                .event(cbEvent.getEventType().name())
                                .data(circuitBreakerEventDataFn.apply(cbEvent, getCircuitBreaker(cbEvent.getCircuitBreakerName())))
                                .build()
                );
    }

    private BiFunction<CircuitBreakerEvent, CircuitBreaker, String> getCircuitBreakerEventStringFunction() {
        return (cbEvent, cb) -> {
            try {
                return jsonMapper.writeValueAsString(
                        new CircuitBreakerHystrixStreamEventsDTO(cbEvent, cb.getState(), cb.getMetrics(), cb.getCircuitBreakerConfig())
                );
            } catch (JsonProcessingException e) {
                // nothing to do
            }
            return "";
        };
    }

    private CircuitBreaker getCircuitBreaker(String circuitBreakerName) {
        return cbr.circuitBreaker(circuitBreakerName);
    }

    private Flux<ServerSentEvent<String>> getHeartbeatStream() {
        return Flux.interval(Duration.ofSeconds(1)).map(i -> ServerSentEvent.<String>builder().event("ping").build());
    }
}
