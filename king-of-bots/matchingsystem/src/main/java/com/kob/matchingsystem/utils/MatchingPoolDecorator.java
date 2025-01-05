package com.kob.matchingsystem.utils;

/**
 * zzy：MatchingPoolDecorator接口定义了所有装饰器类需要实现的方法。
 * 它将提供一种方式来通过装饰器模式扩展原有的匹配池功能。
 */
public interface MatchingPoolDecorator {
    void addPlayer(Integer userId, Integer rating, Integer botId); // zzy：添加玩家
    void removePlayer(Integer userId); // zzy：移除玩家
    void increasingWaitingTime(); // zzy：增加所有玩家的等待时间
    void matchPlayers(); // zzy：尝试匹配所有玩家
    void run(); // zzy：启动匹配池的执行
}

