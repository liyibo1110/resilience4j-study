package com.github.liyibo1110.resilience4j.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用$name-%d格式作为name的ThreadFactory实现
 * @author liyibo
 * @date 2026-02-03 23:27
 */
public class NamingThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefix;

    public NamingThreadFactory(String name) {
        this.group = this.getThreadGroup();
        // 形如name-
        this.prefix = String.join("-", name, "");
    }

    private ThreadGroup getThreadGroup() {
        // 这里不重要，凑合用
        SecurityManager security = System.getSecurityManager();
        return security != null
                ? security.getThreadGroup()
                : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.group, r, this.createName(), 0);
        if(t.isDaemon())
            t.setDaemon(false);
        if(t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

    private String createName() {
        return this.prefix + this.threadNumber.getAndIncrement();
    }
}
