package com.github.liyibo1110.resilience4j.reactor;

import org.reactivestreams.Publisher;

/**
 * @author liyibo
 * @date 2026-02-10 17:44
 */
public class IllegalPublisherException extends IllegalStateException {
    public IllegalPublisherException(Publisher publisher) {
        super("Publisher of type <" + publisher.getClass().getSimpleName() + "> is not supported by this operator");
    }
}
