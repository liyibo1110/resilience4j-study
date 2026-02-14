package com.github.liyibo1110.resilience4j.fallback.autoconfigure;

import com.github.liyibo1110.resilience4j.fallback.CompletionStageFallbackDecorator;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorator;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.ReactorFallbackDecorator;
import com.github.liyibo1110.resilience4j.fallback.configure.FallbackConfiguration;
import com.github.liyibo1110.resilience4j.utils.ReactorOnClasspathCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 如果业务项目引入了spring-boot2模块，spring模块里面的FallbackConfiguration其实是不会生效的（只是个普通的Java类，因为默认不会被扫描到），
 * 因此基于SpringBoot的项目，各种组件的Bean实际是在这些XXXConfigurationOnMissingBean里面生成的（前提是用户没有自定义这些Bean）。
 * @author liyibo
 * @date 2026-02-13 12:10
 */
@Configuration
public class FallbackConfigurationOnMissingBean {
    private final FallbackConfiguration fallbackConfiguration;

    public FallbackConfigurationOnMissingBean() {
        this.fallbackConfiguration = new FallbackConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public FallbackDecorators fallbackDecorators(@Autowired(required = false) List<FallbackDecorator> fallbackDecorators) {
        return fallbackConfiguration.fallbackDecorators(fallbackDecorators);
    }

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorFallbackDecorator reactorFallbackDecorator() {
        return fallbackConfiguration.reactorFallbackDecorator();
    }

    @Bean
    @ConditionalOnMissingBean
    public CompletionStageFallbackDecorator completionStageFallbackDecorator() {
        return fallbackConfiguration.completionStageFallbackDecorator();
    }
}
