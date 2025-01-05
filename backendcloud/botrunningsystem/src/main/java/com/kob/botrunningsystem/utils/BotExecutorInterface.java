package com.kob.botrunningsystem.utils;

//ljp修改：下面接口是机器人产品类要实现的接口
public interface BotExecutorInterface {
    Integer nextMove(String code, String input);
}
