package com.github.liyibo1110.resilience4j.bulkhead.autoconfigure;

import com.github.liyibo1110.resilience4j.bulkhead.Bulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.BulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkhead;
import com.github.liyibo1110.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import com.github.liyibo1110.resilience4j.bulkhead.configure.BulkheadAspect;
import com.github.liyibo1110.resilience4j.bulkhead.configure.BulkheadAspectExt;
import com.github.liyibo1110.resilience4j.bulkhead.configure.BulkheadConfiguration;
import com.github.liyibo1110.resilience4j.bulkhead.configure.BulkheadConfigurationProperties;
import com.github.liyibo1110.resilience4j.bulkhead.configure.ReactorBulkheadAspectExt;
import com.github.liyibo1110.resilience4j.bulkhead.configure.threadpool.ThreadPoolBulkheadConfiguration;
import com.github.liyibo1110.resilience4j.bulkhead.event.BulkheadEvent;
import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigCustomizer;
import com.github.liyibo1110.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigurationProperties;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.spelresolver.autoconfigure.SpelResolverConfigurationOnMissingBean;
import com.github.liyibo1110.resilience4j.utils.AspectJOnClasspathCondition;
import com.github.liyibo1110.resilience4j.utils.ReactorOnClasspathCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Optional;

/**
 * @author liyibo
 * @date 2026-02-13 14:00
 */
@Configuration
@Import({FallbackConfigurationOnMissingBean.class, SpelResolverConfigurationOnMissingBean.class})
public class AbstractBulkheadConfigurationOnMissingBean {
    protected final BulkheadConfiguration bulkheadConfiguration;
    protected final ThreadPoolBulkheadConfiguration threadPoolBulkheadConfiguration;

    public AbstractBulkheadConfigurationOnMissingBean() {
        this.threadPoolBulkheadConfiguration = new ThreadPoolBulkheadConfiguration();
        this.bulkheadConfiguration = new BulkheadConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeBulkheadCustomizer")
    @Qualifier("compositeBulkheadCustomizer")
    public CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer(
            @Autowired(required = false) List<BulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry(
            BulkheadConfigurationProperties bulkheadConfigurationProperties,
            EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
            RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
            @Qualifier("compositeBulkheadCustomizer") CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
        return bulkheadConfiguration.bulkheadRegistry(bulkheadConfigurationProperties, bulkheadEventConsumerRegistry,
                        bulkheadRegistryEventConsumer, compositeBulkheadCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<Bulkhead>>> optionalRegistryEventConsumers) {
        return bulkheadConfiguration.bulkheadRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public BulkheadAspect bulkheadAspect(
            BulkheadConfigurationProperties bulkheadConfigurationProperties,
            ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
            BulkheadRegistry bulkheadRegistry,
            @Autowired(required = false) List<BulkheadAspectExt> bulkHeadAspectExtList,
            FallbackDecorators fallbackDecorators, SpelResolver spelResolver) {
        return bulkheadConfiguration.bulkheadAspect(bulkheadConfigurationProperties, threadPoolBulkheadRegistry,
                        bulkheadRegistry, bulkHeadAspectExtList, fallbackDecorators, spelResolver);
    }

    @Bean
    @Conditional(value={ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorBulkheadAspectExt reactorBulkHeadAspectExt() {
        return bulkheadConfiguration.reactorBulkHeadAspectExt();
    }


    @Bean
    @ConditionalOnMissingBean(name="compositeThreadPoolBulkheadCustomizer")
    @Qualifier("compositeThreadPoolBulkheadCustomizer")
    public CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer(
            @Autowired(required = false) List<ThreadPoolBulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }


    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry(
            ThreadPoolBulkheadConfigurationProperties threadPoolBulkheadConfigurationProperties,
            EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
            RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
            @Qualifier("compositeThreadPoolBulkheadCustomizer") CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
        return threadPoolBulkheadConfiguration.threadPoolBulkheadRegistry(
                threadPoolBulkheadConfigurationProperties, bulkheadEventConsumerRegistry,
                threadPoolBulkheadRegistryEventConsumer, compositeThreadPoolBulkheadCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<ThreadPoolBulkhead>>> optionalRegistryEventConsumers) {
        return threadPoolBulkheadConfiguration.threadPoolBulkheadRegistryEventConsumer(optionalRegistryEventConsumers);
    }
}
