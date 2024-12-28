package com.kob.botrunningsystem;

import com.kob.botrunningsystem.service.impl.BotRunningServiceImpl;
import com.kob.botrunningsystem.utils.BotPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotRunningSystemApplication {
    public static void main(String[] args) {
        //ljp修改：下面语句，通过get函数得到实例
        BotPool.getBotPool().start();
        SpringApplication.run(BotRunningSystemApplication.class, args);
    }
}