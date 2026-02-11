package com.github.liyibo1110.resilience4j.circuitbreaker.monitoring.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.github.liyibo1110.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import com.github.liyibo1110.resilience4j.common.circuitbreaker.monitoring.endpoint.CircuitBreakerEventDTOFactory;
import com.github.liyibo1110.resilience4j.reactor.adapter.ReactorAdapter;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Function;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * 用于产生基于流的CircuitBreaker事件
 * @author liyibo
 * @date 2026-02-10 17:14
 */
@Endpoint(id="streamcircuitbreakerevents")
public class CircuitBreakerServerSideEvent {
    private final CircuitBreakerRegistry cbr;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public CircuitBreakerServerSideEvent(CircuitBreakerRegistry cbr) {
        this.cbr = cbr;
    }

    @ReadOperation(produces=TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getAllCircuitBreakerServerSideEvent() {
        Seq<Flux<CircuitBreakerEvent>> eventStreams = cbr.getAllCircuitBreakers().map(cb -> ReactorAdapter.toFlux(cb.getEventPublisher()));
        return Flux.merge(publishEvents(Array.ofAll(eventStreams)), getHeartbeatStream());
    }

    @ReadOperation(produces=TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getEventsFilteredByCircuitBreakerName(@Selector String name) {
        CircuitBreaker cb = getCircuitBreaker(name);
        Flux<CircuitBreakerEvent> eventStream = ReactorAdapter.toFlux(cb.getEventPublisher());
        return Flux.merge(publishEvents(Array.of(eventStream)), getHeartbeatStream());
    }

    @ReadOperation(produces=TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getEventsFilteredByCircuitBreakerNameAndEventType(@Selector String name,
                                                                                           @Selector String eventType) {
        CircuitBreaker cb = getCircuitBreaker(name);
        Flux<CircuitBreakerEvent> eventStream = ReactorAdapter.toFlux(cb.getEventPublisher())
                .filter(event -> event.getEventType() == CircuitBreakerEvent.Type.valueOf(eventType.toUpperCase()));
        return Flux.merge(publishEvents(Array.of(eventStream)), getHeartbeatStream());
    }

    /**
     * 重要：将收到的CircuitBreakerEvent实例，最后转换成框架可直接输出的ServerSentEvent形式
     */
    private Flux<ServerSentEvent<String>> publishEvents(Seq<Flux<CircuitBreakerEvent>> eventStreams) {
        Function<CircuitBreakerEvent, String> circuitBreakerEventDataFn = getCircuitBreakerEventStringFunction();
        return Flux.merge(eventStreams)
                .onBackpressureDrop()
                .delayElements(Duration.ofMillis(100))
                .map(cbe -> ServerSentEvent.<String>builder()
                        .id(cbe.getCircuitBreakerName())
                        .event(cbe.getEventType().name())
                        .data(circuitBreakerEventDataFn.apply(cbe))
                        .build());
    }

    private Function<CircuitBreakerEvent, String> getCircuitBreakerEventStringFunction() {
        return cbe -> {
            try {
                return jsonMapper.writeValueAsString(CircuitBreakerEventDTOFactory.createCircuitBreakerEventDTO(cbe));
            } catch (JsonProcessingException e) {
                // nothing to do
            }
            return "";
        };
    }

    private CircuitBreaker getCircuitBreaker(String circuitBreakerName) {
        return cbr.circuitBreaker(circuitBreakerName);
    }

    /**
     * 返回每秒生产一个ping字符串的心跳Flux
     */
    private Flux<ServerSentEvent<String>> getHeartbeatStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> ServerSentEvent.<String>builder().event("ping").build());
    }
}
