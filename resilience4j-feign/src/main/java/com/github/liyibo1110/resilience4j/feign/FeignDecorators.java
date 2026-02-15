package com.github.liyibo1110.resilience4j.feign;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.circuitbreaker.CircuitBreaker;
import com.github.liyibo1110.resilience4j.ratelimiter.RateLimiter;
import com.github.liyibo1110.resilience4j.retry.Retry;
import feign.InvocationHandlerFactory;
import feign.Target;
import io.vavr.CheckedFunction1;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author liyibo
 * @date 2026-02-14 14:26
 */
public class FeignDecorators implements FeignDecorator {
    private final List<FeignDecorator> decorators;

    private FeignDecorators(List<FeignDecorator> decorators) {
        this.decorators = decorators;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public CheckedFunction1<Object[], Object> decorate(CheckedFunction1<Object[], Object> fn, Method method,
                                                       InvocationHandlerFactory.MethodHandler methodHandler, Target<?> target) {
        CheckedFunction1<Object[], Object> decoratedFn = fn;
        for(final FeignDecorator decorator : decorators)
            decoratedFn = decorator.decorate(decoratedFn, method, methodHandler, target);
        return decoratedFn;
    }

    public static final class Builder {
        private final List<FeignDecorator> decorators = new ArrayList<>();

        public Builder withRetry(Retry retry) {
            this.addFeignDecorator(fn -> Retry.decorateCheckedFunction(retry, fn));
            return this;
        }

        public Builder withCircuitBreaker(CircuitBreaker cb) {
            this.addFeignDecorator(fn -> CircuitBreaker.decorateCheckedFunction(cb, fn));
            return this;
        }

        public Builder withRateLimiter(RateLimiter rateLimiter) {
            this.addFeignDecorator(fn -> RateLimiter.decorateCheckedFunction(rateLimiter, fn));
            return this;
        }

        public Builder withBulkhead(Bulkhead bulkhead) {
            this.addFeignDecorator(fn -> Bulkhead.decorateCheckedFunction(bulkhead, fn));
            return this;
        }

        public Builder withFallback(Object fallback) {
            this.decorators.add(new FallbackDecorator<>(new DefaultFallbackHandler<>(fallback)));
            return this;
        }

        public Builder withFallbackFactory(Function<Exception, ?> fallbackSupplier) {
            this.decorators.add(new FallbackDecorator<>(new FallbackFactory<>(fallbackSupplier)));
        }

        public Builder withFallback(Object fallback, Class<? extends Exception> filter) {
            this.decorators.add(new FallbackDecorator<>(new DefaultFallbackHandler<>(fallback), filter));
            return this;
        }

        public Builder withFallbackFactory(Function<Exception, ?> fallbackSupplier, Class<? extends Exception> filter) {
            this.decorators.add(new FallbackDecorator<>(new FallbackFactory<>(fallbackSupplier), filter));
            return this;
        }

        public Builder withFallback(Object fallback, Predicate<Exception> filter) {
            this.decorators.add(new FallbackDecorator<>(new DefaultFallbackHandler<>(fallback), filter));
            return this;
        }

        public Builder withFallbackFactory(Function<Exception, ?> fallbackSupplier, Predicate<Exception> filter) {
            this.decorators.add(new FallbackDecorator<>(new FallbackFactory<>(fallbackSupplier), filter));
            return this;
        }

        private void addFeignDecorator(UnaryOperator<CheckedFunction1<Object[], Object>> decorator) {
            /**
             * fn对应的是原始Feign MethodHandler调用，它内部负责发http请求，相当于methodHandler.invoke(args)的invoke方法
             * mh对应的是MethodHandler，即Feign的http原生执行器
             */
            this.decorators.add((fn, m, mh, t) -> {
                if(m.isDefault())   // 排除default方法，相当于直接调用原始MethodHandler的invoke
                    return fn;
                else    // 用传入的decorator套一层MethodHandler调用，这里其实没用到mh和t，是留给FallbackDecorator用的
                    return decorator.apply(fn);
            });
        }

        public FeignDecorators build() {
            return new FeignDecorators(decorators);
        }
    }
}
