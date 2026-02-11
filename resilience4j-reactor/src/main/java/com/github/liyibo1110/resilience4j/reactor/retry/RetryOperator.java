package com.github.liyibo1110.resilience4j.reactor.retry;

import com.github.liyibo1110.resilience4j.reactor.IllegalPublisherException;
import com.github.liyibo1110.resilience4j.retry.Retry;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.UnaryOperator;

/**
 * @author liyibo
 * @date 2026-02-10 23:06
 */
public class RetryOperator<T> implements UnaryOperator<Publisher<T>> {
    private final Retry retry;

    private RetryOperator(Retry retry) {
        this.retry = retry;
    }

    public static <T> RetryOperator<T> of(Retry retry) {
        return new RetryOperator<>(retry);
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if(publisher instanceof Mono) {
            Context<T> context = new Context<>(retry.asyncContext());
            Mono<T> upstream = (Mono<T>)publisher;
            return upstream.doOnNext(context::handleResult)
                    .retryWhen(reactor.util.retry.Retry.withThrowable(errors -> errors.flatMap(context::handleErrors)))
                    .doOnSuccess(t -> context.onComplete());
        }else if(publisher instanceof Flux) {
            Context<T> context = new Context<>(retry.asyncContext());
            Flux<T> upstream = (Flux<T>)publisher;
            return upstream.doOnNext(context::handleResult)
                    .retryWhen(reactor.util.retry.Retry.withThrowable(errors -> errors.flatMap(context::handleErrors)))
                    .doOnComplete(context::onComplete);
        }else {
            throw new IllegalPublisherException(publisher);
        }
    }

    private static class Context<T> {
        private final Retry.AsyncContext<T> retryContext;

        Context(Retry.AsyncContext<T> retryContext) {
            this.retryContext = retryContext;
        }

        void onComplete() {
            this.retryContext.onComplete();
        }

        void handleResult(T result) {
            long waitDurationMillis = retryContext.onResult(result);
            if (waitDurationMillis != -1) {
                throw new RetryDueToResultException(waitDurationMillis);
            }
        }

        Publisher<Long> handleErrors(Throwable throwable) {
            if(throwable instanceof RetryDueToResultException) {
                long waitDurationMillis = ((RetryDueToResultException) throwable).waitDurationMillis;
                return Mono.delay(Duration.ofMillis(waitDurationMillis));
            }
            // Filter Error to not retry on it
            if(throwable instanceof Error)
                throw (Error) throwable;

            long waitDurationMillis = retryContext.onError(throwable);

            if(waitDurationMillis == -1)
                return Mono.error(throwable);

            return Mono.delay(Duration.ofMillis(waitDurationMillis));
        }

        private static class RetryDueToResultException extends RuntimeException {
            private final long waitDurationMillis;

            RetryDueToResultException(long waitDurationMillis) {
                super("retry due to retryOnResult predicate");
                this.waitDurationMillis = waitDurationMillis;
            }
        }
    }
}
