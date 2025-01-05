package com.kob.botrunningsystem.utils;

import org.springframework.stereotype.Component;

//ljp修改 java具体工厂实现
@Component
public class JavaExecutorFactory implements BotFactoryInterface {
    @Override
    public BotExecutorInterface createBot() {
        return new JavaBotExecutor();
    }
}
