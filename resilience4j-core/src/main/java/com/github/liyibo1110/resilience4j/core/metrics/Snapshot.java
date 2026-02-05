package com.github.liyibo1110.resilience4j.core.metrics;

import java.time.Duration;

/**
 * 统计快照
 * @author liyibo
 * @date 2026-02-04 17:55
 */
public interface Snapshot {

    /**
     * 返回所有call的总用时
     */
    Duration getTotalDuration();

    /**
     * 返回所有call的平均用时
     */
    Duration getAverageDuration();

    /**
     * 返回慢于特定阈值call总次数
     */
    int getTotalNumberOfSlowCalls();

    /**
     * 返回慢于特定阈值call总成功次数
     */
    int getNumberOfSlowSuccessfulCalls();

    /**
     * 返回慢于特定阈值call总失败次数
     */
    int getNumberOfSlowFailedCalls();

    /**
     * 返回忙于特定阈值call的总占比
     */
    float getSlowCallRate();

    /**
     * 返回call总成功次数
     */
    int getNumberOfSuccessfulCalls();

    /**
     * 返回call总失败次数
     */
    int getNumberOfFailedCalls();

    /**
     * 返回call总数
     */
    int getTotalNumberOfCalls();

    /**
     * 返回call的失败率
     */
    float getFailureRate();
}
