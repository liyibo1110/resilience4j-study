package com.github.liyibo1110.resilience4j.retry.configure;

import com.github.liyibo1110.resilience4j.common.CompositeCustomizer;
import com.github.liyibo1110.resilience4j.common.timelimiter.configuration.TimeLimiterConfigCustomizer;
import com.github.liyibo1110.resilience4j.consumer.DefaultEventConsumerRegistry;
import com.github.liyibo1110.resilience4j.consumer.EventConsumerRegistry;
import com.github.liyibo1110.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import com.github.liyibo1110.resilience4j.core.registry.CompositeRegistryEventConsumer;
import com.github.liyibo1110.resilience4j.core.registry.RegistryEventConsumer;
import com.github.liyibo1110.resilience4j.fallback.FallbackDecorators;
import com.github.liyibo1110.resilience4j.spelresolver.SpelResolver;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiter;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterConfig;
import com.github.liyibo1110.resilience4j.timelimiter.TimeLimiterRegistry;
import com.github.liyibo1110.resilience4j.timelimiter.configure.ReactorTimeLimiterAspectExt;
import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterAspect;
import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterAspectExt;
import com.github.liyibo1110.resilience4j.timelimiter.configure.TimeLimiterConfigurationProperties;
import com.github.liyibo1110.resilience4j.timelimiter.event.TimeLimiterEvent;
import com.github.liyibo1110.resilience4j.utils.AspectJOnClasspathCondition;
import com.github.liyibo1110.resilience4j.utils.ReactorOnClasspathCondition;
import io.vavr.collection.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-02-13 11:57
 */
@Configuration
public class TimeLimiterConfiguration {

    @Bean
    @Qualifier("compositeTimeLimiterCustomizer")
    public CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer(
            @Autowired(required = false) List<TimeLimiterConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry(
            TimeLimiterConfigurationProperties prop,
            EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventConsumerRegistry,
            RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
            @Qualifier("compositeTimeLimiterCustomizer") CompositeCustomizer<TimeLimiterConfigCustomizer> customizer) {
        TimeLimiterRegistry timeLimiterRegistry = createTimeLimiterRegistry(prop, timeLimiterRegistryEventConsumer, customizer);
        registerEventConsumer(timeLimiterRegistry, timeLimiterEventConsumerRegistry, prop);

        initTimeLimiterRegistry(timeLimiterRegistry, prop, customizer);
        return timeLimiterRegistry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer(Optional<List<RegistryEventConsumer<TimeLimiter>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    @Bean
    @Conditional(AspectJOnClasspathCondition.class)
    public TimeLimiterAspect timeLimiterAspect(
            TimeLimiterConfigurationProperties prop, TimeLimiterRegistry timeLimiterRegistry,
            @Autowired(required = false) List<TimeLimiterAspectExt> timeLimiterAspectExtList,
            FallbackDecorators fallbackDecorators, SpelResolver spelResolver,
            @Autowired(required = false) ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor) {
        return new TimeLimiterAspect(timeLimiterRegistry, prop, timeLimiterAspectExtList, fallbackDecorators, spelResolver, contextAwareScheduledThreadPoolExecutor);
    }

    @Bean
    @Conditional({ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public ReactorTimeLimiterAspectExt reactorTimeLimiterAspectExt() {
        return new ReactorTimeLimiterAspectExt();
    }

    @Bean
    public EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventsConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }

    private static TimeLimiterRegistry createTimeLimiterRegistry(
            TimeLimiterConfigurationProperties prop, RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
            CompositeCustomizer<TimeLimiterConfigCustomizer> customizer) {
        Map<String, TimeLimiterConfig> configs = prop.getConfigs()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> prop.createTimeLimiterConfig(entry.getKey(), entry.getValue(), customizer)));

        return TimeLimiterRegistry.of(configs, timeLimiterRegistryEventConsumer, HashMap.ofAll(prop.getTags()));
    }

    void initTimeLimiterRegistry(TimeLimiterRegistry timeLimiterRegistry,
            TimeLimiterConfigurationProperties prop, CompositeCustomizer<TimeLimiterConfigCustomizer> customizer) {

        prop.getInstances().forEach(
                (name, properties) -> timeLimiterRegistry.timeLimiter(name, prop.createTimeLimiterConfig(name, properties, customizer))
        );
    }

    private static void registerEventConsumer(TimeLimiterRegistry timeLimiterRegistry,
                                              EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry,
                                              TimeLimiterConfigurationProperties prop) {
        timeLimiterRegistry.getEventPublisher()
                .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), prop))
                .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), prop));
    }

    private static void registerEventConsumer(EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry, TimeLimiter timeLimiter,
                                              TimeLimiterConfigurationProperties prop) {
        int eventConsumerBufferSize = Optional.ofNullable(prop.getInstanceProperties(timeLimiter.getName()))
                .map(com.github.liyibo1110.resilience4j.common.timelimiter.configuration.TimeLimiterConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                .orElse(100);
        timeLimiter.getEventPublisher().onEvent(eventConsumerRegistry.createEventConsumer(timeLimiter.getName(), eventConsumerBufferSize));
    }
}
