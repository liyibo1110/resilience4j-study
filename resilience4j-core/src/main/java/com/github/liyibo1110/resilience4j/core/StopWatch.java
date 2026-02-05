package com.github.liyibo1110.resilience4j.core;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * 简单的计时工具，只能统计开始和停止之间的时间段值，内部使用的是JDK自带的Clock组件
 * @author liyibo
 * @date 2026-02-02 17:46
 */
public class StopWatch {
    private final Instant startTime;
    private Clock clock;

    StopWatch(Clock clock) {
        this.startTime = clock.instant();
        this.clock = clock;
    }

    public static StopWatch start() {
        return new StopWatch(Clock.systemUTC());
    }

    public Duration stop() {
        return Duration.between(this.startTime, this.clock.instant());
    }
}
