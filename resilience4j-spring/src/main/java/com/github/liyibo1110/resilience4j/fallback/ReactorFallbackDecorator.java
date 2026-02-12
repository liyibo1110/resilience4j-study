package com.github.liyibo1110.resilience4j.fallback;

import com.github.liyibo1110.resilience4j.utils.AspectUtil;
import io.vavr.CheckedFunction0;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.function.Function;

/**
 * 专门用在reactor流上面的FallbackDecorator实现
 * @author liyibo
 * @date 2026-02-12 17:36
 */
public class ReactorFallbackDecorator implements FallbackDecorator {
    /** 返回值是Mono/Flux的符合匹配要求 */
    private static final Set<Class<?>> REACTORS_SUPPORTED_TYPES = AspectUtil.newHashSet(Mono.class, Flux.class);

    @Override
    public boolean supports(Class<?> target) {
        return REACTORS_SUPPORTED_TYPES.stream().anyMatch(type -> type.isAssignableFrom(target));
    }

    @Override
    public CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod, CheckedFunction0<Object> supplier) {
        return supplier.andThen(returnValue -> {
            if(Flux.class.isAssignableFrom(returnValue.getClass())) {   // 返回值是Flux及其子类
                Flux fluxReturnValue = (Flux)returnValue;
                return fluxReturnValue.onErrorResume(reactorOnErrorResume(fallbackMethod, Flux::error));
            }else if(Mono.class.isAssignableFrom(returnValue.getClass())) { // 返回值是Mono及其子类
                Mono monoReturnValue = (Mono)returnValue;
                return monoReturnValue.onErrorResume(reactorOnErrorResume(fallbackMethod, Mono::error));
            }else { // 返回值不是Flux或Mono自身或者其子类，则直接返回原始返回值
                return returnValue;
            }
        });
    }

    /**
     * 将FallbackMethod和特定reactor的错误处理Flux包装成Function，当作onErrorResume的参数
     */
    private <T> Function<? super Throwable, ? extends Publisher<? extends T>> reactorOnErrorResume(
            FallbackMethod fallbackMethod,
            Function<? super Throwable, ? extends Publisher<? extends T>> errorFunction) {
        return t -> {
            try {
                return (Publisher<? extends T>)fallbackMethod.fallback(t);
            } catch (Throwable fallbackThrowable) {
                return errorFunction.apply(fallbackThrowable);
            }
        };
    }
}
