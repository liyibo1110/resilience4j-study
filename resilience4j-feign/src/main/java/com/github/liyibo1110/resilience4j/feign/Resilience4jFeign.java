package com.github.liyibo1110.resilience4j.feign;

import feign.Feign;
import feign.InvocationHandlerFactory;

/**
 * 包装了Feign.Builder类
 * @author liyibo
 * @date 2026-02-14 17:35
 */
public final class Resilience4jFeign {

    public static Builder builder(FeignDecorator invocationDecorator) {
        return new Builder(invocationDecorator);
    }

    public static final class Builder extends Feign.Builder {
        /** 注意这个FeignDecorator，其实可能是个FeignDecorators，即内部包含一堆FeignDecorator */
        private final FeignDecorator invocationDecorator;

        public Builder(FeignDecorator invocationDecorator) {
            this.invocationDecorator = invocationDecorator;
        }

        @Override
        public Feign.Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Feign build() {
            // 重点：这里set了一个自定义的InvocationHandlerFactory实现，内容是new了自己扩展的InvocationHandler实现
            super.invocationHandlerFactory((target, dispatch) -> new DecoratorInvocationHandler(target, dispatch, invocationDecorator));
            return super.build();
        }
    }
}
