package com.kob.matchingsystem.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * zzy：MatchingPool类作为系统的统一入口，
 * 通过装饰器模式增强匹配池的功能。
 */
@Component
public class MatchingPool extends Thread {
    private final MatchingPoolDecorator matchingPool;

    // zzy：构造函数，注入装饰的匹配池实例
    @Autowired
    public MatchingPool(BaseMatchingPool baseMatchingPool) {
        this.matchingPool = new MatchingPoolLoggingDecorator(baseMatchingPool); // zzy：通过装饰器增强功能
    }

    // zzy：对外提供的添加玩家接口
    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        matchingPool.addPlayer(userId, rating, botId);
    }

    // zzy：对外提供的移除玩家接口
    public void removePlayer(Integer userId) {
        matchingPool.removePlayer(userId);
    }

    // zzy：启动匹配池执行
    @Override
    public void run() {
        matchingPool.run();
    }
}
