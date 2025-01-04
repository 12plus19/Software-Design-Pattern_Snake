package com.kob.matchingsystem.utils;

/**
 * zzy：MatchingPoolLoggingDecorator为匹配池增加日志记录功能。
 * zzy：每次操作（如添加玩家、移除玩家等）都会在控制台输出日志。
 */
public class MatchingPoolLoggingDecorator implements MatchingPoolDecorator {
    private MatchingPoolDecorator matchingPool; // zzy：被装饰的原始匹配池

    // zzy：构造函数，接收一个MatchingPoolDecorator实例作为参数
    public MatchingPoolLoggingDecorator(MatchingPoolDecorator matchingPool) {
        this.matchingPool = matchingPool;
    }

    // zzy：添加玩家时输出日志
    @Override
    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        System.out.println("Adding player: userId=" + userId + ", rating=" + rating + ", botId=" + botId); // zzy：日志记录
        matchingPool.addPlayer(userId, rating, botId); // zzy：调用被装饰的addPlayer方法
    }

    // zzy：移除玩家时输出日志
    @Override
    public void removePlayer(Integer userId) {
        System.out.println("Removing player: userId=" + userId); // zzy：日志记录
        matchingPool.removePlayer(userId); // zzy：调用被装饰的removePlayer方法
    }

    // zzy：增加等待时间时输出日志
    @Override
    public void increasingWaitingTime() {
        System.out.println("Increasing waiting time for all players"); // zzy：日志记录
        matchingPool.increasingWaitingTime(); // zzy：调用被装饰的increasingWaitingTime方法
    }

    // zzy：匹配玩家时输出日志
    @Override
    public void matchPlayers() {
        System.out.println("Attempting to match players"); // zzy：日志记录
        matchingPool.matchPlayers(); // zzy：调用被装饰的matchPlayers方法
    }

    // zzy：启动匹配池的执行时输出日志
    @Override
    public void run() {
        System.out.println("Starting matching pool execution"); // zzy：日志记录
        matchingPool.run(); // zzy：调用被装饰的run方法
    }
}
