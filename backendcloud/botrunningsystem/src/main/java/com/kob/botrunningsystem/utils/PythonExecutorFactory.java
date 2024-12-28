package com.kob.botrunningsystem.utils;

import org.springframework.stereotype.Component;

//ljp修改python代码执行器工厂
@Component
public class PythonExecutorFactory implements BotFactoryInterface {
    @Override
    public BotExecutorInterface createBot() {
        return new PythonBotExecutor();
    }
}