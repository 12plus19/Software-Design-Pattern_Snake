package com.kob.botrunningsystem.utils;

/**
 * zzy: 具体策略类，使用超时机制执行Bot
 */
public class TimeoutExecutionStrategy implements BotExecutionStrategy {

    @Override
    public void execute(Bot bot) {
        // zzy: 调用Consumer工具类执行Bot，并设置超时时间为2000ms
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);
    }
}
