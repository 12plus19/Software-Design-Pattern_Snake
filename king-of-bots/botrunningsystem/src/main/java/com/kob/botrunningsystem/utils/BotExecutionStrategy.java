package com.kob.botrunningsystem.utils;

/**
 * zzy: 定义策略接口，提供统一的Bot执行方式
 */
public interface BotExecutionStrategy {
    /**
     * zzy: 定义执行方法
     * @param bot 需要执行的Bot对象
     */
    void execute(Bot bot);
}
