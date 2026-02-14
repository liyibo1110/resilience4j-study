package com.github.liyibo1110.resilience4j.spelresolver.autoconfigure;

import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.spelresolver.configure.SpelResolverConfiguration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author liyibo
 * @date 2026-02-13 13:00
 */
@Configuration
public class SpelResolverConfigurationOnMissingBean {

    private final SpelResolverConfiguration configuration;

    public SpelResolverConfigurationOnMissingBean() {
        this.configuration = new SpelResolverConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return configuration.parameterNameDiscoverer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpelExpressionParser spelExpressionParser() {
        return configuration.spelExpressionParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpelResolver spelResolver(SpelExpressionParser spelExpressionParser,
                                     ParameterNameDiscoverer parameterNameDiscoverer,
                                     BeanFactory beanFactory) {
        return configuration.spelResolver(spelExpressionParser, parameterNameDiscoverer, beanFactory);
    }
}
