package com.kob.botrunningsystem.service.impl;

import com.kob.botrunningsystem.service.BotRunningService;
import com.kob.botrunningsystem.utils.BotPool;
import org.springframework.stereotype.Service;

@Service
public class BotRunningServiceImpl implements BotRunningService {
    //ljp修改：下面语句，通过get函数得到实例，而不是通过构造函数构造
    public final static BotPool botPool = BotPool.getBotPool();

    @Override
    public String addBot(Integer userId, Integer botId, String botCode, String input) {
        botPool.addBot(userId, botId, botCode, input);
        System.out.println(botCode);
        return "add bot successfully";
    }
}
