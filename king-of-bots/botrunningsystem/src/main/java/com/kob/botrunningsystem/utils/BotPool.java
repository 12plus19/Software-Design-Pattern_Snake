package com.kob.botrunningsystem.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zzy: BotPool类，使用线程安全队列管理Bot任务
 */
public class BotPool extends Thread {
    private final ReentrantLock lock = new ReentrantLock(); // zzy: 锁对象
    private final Condition condition = lock.newCondition(); // zzy: 条件变量
    private final Queue<Bot> bots = new LinkedList<>(); // zzy: 消息队列

    private BotExecutionStrategy botExecutionStrategy; // zzy: 策略对象

    /**
     * zzy: 设置策略对象
     * @param botExecutionStrategy 具体的策略实现
     */
    public void setBotExecutionStrategy(BotExecutionStrategy botExecutionStrategy) {
        this.botExecutionStrategy = botExecutionStrategy;
    }

    /**
     * zzy: 添加Bot任务到队列，并唤醒等待线程
     * @param userId 用户ID
     * @param botCode Bot代码
     * @param input Bot输入信息
     */
    public void addBot(Integer userId, String botCode, String input) {
        lock.lock(); // zzy: 获取锁
        try {
            bots.add(new Bot(userId, botCode, input)); // zzy: 将Bot任务添加到队列
            condition.signalAll(); // zzy: 唤醒等待线程
        } finally {
            lock.unlock(); // zzy: 释放锁
        }
    }

    /**
     * zzy: 消费Bot任务，调用策略执行
     * @param bot Bot对象
     */
    private void consume(Bot bot) {
        if (botExecutionStrategy != null) {
            botExecutionStrategy.execute(bot); // zzy: 使用当前策略执行Bot任务
        } else {
            throw new IllegalStateException("BotExecutionStrategy is not set"); // zzy: 策略未设置
        }
    }

    /**
     * zzy: 主线程运行逻辑，持续消费队列中的任务
     */
    @Override
    public void run() {
        while (true) {
            lock.lock(); // zzy: 获取锁
            try {
                if (bots.isEmpty()) {
                    try {
                        condition.await(); // zzy: 等待条件变量
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break; // zzy: 线程中断时退出循环
                    }
                } else {
                    Bot bot = bots.remove(); // zzy: 从队列中取出任务
                    consume(bot); // zzy: 消费任务
                }
            } finally {
                lock.unlock(); // zzy: 释放锁
            }
        }
    }
}
