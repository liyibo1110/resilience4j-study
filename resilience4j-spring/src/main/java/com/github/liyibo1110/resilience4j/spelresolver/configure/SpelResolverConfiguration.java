package com.github.liyibo1110.resilience4j.spelresolver.configure;

import com.github.liyibo1110.resilience4j.spelresolver.DefaultSpelResolver;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author liyibo
 * @date 2026-02-13 00:21
 */
@Configuration
public class SpelResolverConfiguration {

    @Bean
    public SpelResolver spelResolver(SpelExpressionParser parser, ParameterNameDiscoverer parameterNameDiscoverer, BeanFactory beanFactory) {
        return new DefaultSpelResolver(parser, parameterNameDiscoverer, beanFactory);
    }

    @Bean
    public SpelExpressionParser spelExpressionParser() {
        return new SpelExpressionParser();
    }

    @Bean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new StandardReflectionParameterNameDiscoverer();
    }
}
