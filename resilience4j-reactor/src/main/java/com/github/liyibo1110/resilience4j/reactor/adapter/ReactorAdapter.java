package com.github.liyibo1110.resilience4j.reactor.adapter;

import com.github.liyibo1110.resilience4j.core.EventPublisher;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;

/**
 * @author liyibo
 * @date 2026-02-10 17:50
 */
public class ReactorAdapter {
    private ReactorAdapter() {}

    /**
     * 将给定的EventPublisher实例的onEvent回调，转成Flux特定实现返回（DirectProcessor）
     */
    public static <T> Flux<T> toFlux(EventPublisher<T> eventPublisher) {
        DirectProcessor<T> directProcessor = DirectProcessor.create();
        eventPublisher.onEvent(directProcessor::onNext);
        return directProcessor;
    }
}
