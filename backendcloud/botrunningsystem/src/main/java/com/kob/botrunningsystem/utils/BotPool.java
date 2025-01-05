package com.kob.botrunningsystem.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread {
    //ljp修改：下面语句，创建实例，整个类只有一个实例
    private static final BotPool botPool = new BotPool();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<Bot> bots = new LinkedList<>();       // 消息队列
    //ljp修改：下面语句，私有化构造函数，保证只有一个实例
    private BotPool(){}
    //ljp修改：下面语句，需要用到时返回实例，保证只有一个实例
    public static BotPool getBotPool(){
        return botPool;
    }
    public void addBot(Integer userId, Integer botId, String botCode, String input){
        lock.lock();
        try{
            bots.add(new Bot(userId, botId, botCode, input, "java"));
            condition.signalAll();         //  一共两个线程，添加bot后唤醒BotPool线程
        } finally {
            lock.unlock();
        }
    }
    private void consume(Bot bot) {      //   用一个线程去执行，可以控制执行的时间
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);
    }
    @Override
    public void run() {
        while(true){
            lock.lock();
            if(bots.isEmpty()){
                try {
                    condition.await();   //  await包含释放锁的操作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = bots.remove();
                lock.unlock();
                consume(bot);              //  比较耗时，unlock要在前面
            }
        }
    }
}
