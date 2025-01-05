package com.kob.botrunningsystem.utils;

//ljp修改：下面接口是机器人工厂类要实现的接口
public interface BotFactoryInterface {
    BotExecutorInterface createBot();
}

