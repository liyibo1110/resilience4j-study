package com.github.liyibo1110.resilience4j.fallback.configure;

import com.github.liyibo1110.resilience4j.fallback.CompletionStageFallbackDecorator;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorator;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.ReactorFallbackDecorator;
import com.github.liyibo1110.resilience4j.utils.ReactorOnClasspathCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-02-11 15:45
 */
@Configuration
public class FallbackConfiguration {

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class})
    public ReactorFallbackDecorator reactorFallbackDecorator() {
        return new ReactorFallbackDecorator();
    }

    @Bean
    public CompletionStageFallbackDecorator completionStageFallbackDecorator() {
        return new CompletionStageFallbackDecorator();
    }

    @Bean
    public FallbackDecorators fallbackDecorators(@Autowired(required = false) List<FallbackDecorator> fallbackDecorator) {
        return new FallbackDecorators(fallbackDecorator);
    }
}
