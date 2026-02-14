package com.github.liyibo1110.resilience4j.core;

import com.github.liyibo1110.resilience4j.core.lang.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 核心事件模型（事件处理器，也负责绑定消费者）
 * 本质是一个双通道事件订阅模型（存在onEventConsumers这样的List，以及eventConsumerMap这样的map）
 * 线程安全组件
 * @author liyibo
 * @date 2026-02-04 13:47
 */
public class EventProcessor<T> implements EventPublisher<T> {

    /**
     * 通用配置的监听器，监听所有事件，不区分事件子类型，只保存通过onEvent传进来的EventConsumer，主要用于
     * 1、CircuitBreaker事件
     * 2、Retry事件
     * 3、metrics采集
     * 4、调试
     * 使用CopyOnWriteArrayList是因为场景为读多写少
     */
    final List<EventConsumer<T>> onEventConsumers = new CopyOnWriteArrayList<>();

    /**
     * 按事件类型区分的监听器，key是Event对象对应的className，value是对应的消费者列表，通过registerConsumer方法来注册，只监听某类事件例如
     * 1、ERROR
     * 2、STATE_TRANSITION
     * 3、RETRY_ON_ERROR
     */
    final ConcurrentMap<String, List<EventConsumer<T>>> eventConsumerMap = new ConcurrentHashMap<>();

    /** 快速路径优化（fast path），主要用来判断是否为false，即没有消费者，这样就不用再做诸如构造Event实例的事情了 */
    private boolean consumerRegistered;

    public boolean hasConsumers() {
        return consumerRegistered;
    }

    /**
     * 绑定消费者，使用了synchronized不是为了让eventConsumerMap线程安全（因为本来就安全），是为了：
     * 1、和consumerRegistered保持原子一致性
     * 2、避免注册过程发生竞争
     * 3、保证内部List创建过程有原子性语义
     */
    public synchronized void registerConsumer(String className, EventConsumer<? extends T> eventConsumer) {
        this.eventConsumerMap.compute(className, (k, consumers) -> {
            if(consumers == null) { // value == null
                consumers = new CopyOnWriteArrayList<>();
                consumers.add((EventConsumer<T>)eventConsumer);
                return consumers;   // 写入value
            }else { // value已存在，直接追加
                consumers.add((EventConsumer<T>)eventConsumer);
                return consumers;
            }
        });
        this.consumerRegistered = true;
    }

    /**
     * 处理传来的事件，只要2种集合内有1个消费者被发送出去了，即返回true
     */
    public <E extends T> boolean processEvent(E event) {
        boolean consumed = false;
        final List<EventConsumer<T>> onEventConsumers = this.onEventConsumers;
        // 事件来了，先用onEventConsumers发一圈
        if(!onEventConsumers.isEmpty()) {
            for(int i = 0, size = onEventConsumers.size(); i < size; i++)
                onEventConsumers.get(i).consumeEvent(event);
            consumed = true;
        }
        // 再用eventConsumerMap发送
        if(!this.eventConsumerMap.isEmpty()) {
            final List<EventConsumer<T>> consumers = this.eventConsumerMap.get(event.getClass().getName());
            if(consumers != null && !consumers.isEmpty()) {
                for(int i = 0, size = consumers.size(); i < size; i++)
                    consumers.get(i).consumeEvent(event);
                consumed = true;
            }
        }
        return consumed;
    }

    @Override
    public synchronized void onEvent(@Nullable EventConsumer<T> onEventConsumer) {
        // 只有在这里才会给onEventConsumers列表增加EventConsumer
        this.onEventConsumers.add(onEventConsumer);
        this.consumerRegistered = true;
    }
}
