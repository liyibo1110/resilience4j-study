package com.github.liyibo1110.resilience4j.reactor.timelimiter;

import com.github.liyibo1110.resilience4j.reactor.IllegalPublisherException;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.UnaryOperator;

/**
 * @author liyibo
 * @date 2026-02-10 23:10
 */
public class TimeLimiterOperator<T> implements UnaryOperator<Publisher<T>> {
    private final TimeLimiter timeLimiter;

    private TimeLimiterOperator(TimeLimiter timeLimiter) {
        this.timeLimiter = timeLimiter;
    }

    public static <T> TimeLimiterOperator<T> of(TimeLimiter timeLimiter) {
        return new TimeLimiterOperator<>(timeLimiter);
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if(publisher instanceof Mono)
            return withTimeout((Mono<T>) publisher);
        else if (publisher instanceof Flux)
            return withTimeout((Flux<T>) publisher);
        else
            throw new IllegalPublisherException(publisher);
    }

    private Publisher<T> withTimeout(Mono<T> upstream) {
        return upstream.timeout(getTimeout())
                .doOnNext(t -> timeLimiter.onSuccess())
                .doOnSuccess(t -> timeLimiter.onSuccess())
                .doOnError(timeLimiter::onError);
    }

    private Publisher<T> withTimeout(Flux<T> upstream) {
        return upstream.timeout(getTimeout())
                .doOnNext(t -> timeLimiter.onSuccess())
                .doOnComplete(timeLimiter::onSuccess)
                .doOnError(timeLimiter::onError);
    }

    private Duration getTimeout() {
        return timeLimiter.getTimeLimiterConfig().getTimeoutDuration();
    }
}
