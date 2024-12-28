package com.kob.matchingsystem;

import com.kob.matchingsystem.utils.MatchingPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchingSystemApplication {
    public static void main(String[] args) {
        //ljp修改：下面语句，通过get函数得到实例，只有一个实例
        MatchingPool.getMatchingPool().start();
        SpringApplication.run(MatchingSystemApplication.class, args);
    }
}