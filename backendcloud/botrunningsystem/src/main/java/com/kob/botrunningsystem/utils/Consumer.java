package com.kob.botrunningsystem.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;


@Component
public class Consumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";
    //ljp修改：下面通过传来的语言类型，决定使用哪种机器人产品，java或python，之后加入新的语言后也可以动态扩展
    private final Map<String, BotExecutorInterface> executors;
    //ljp修改：下面初始化executors
    public Consumer() {
        this.executors = new HashMap<>();
        this.executors.put("java", new JavaExecutorFactory().createBot());
        this.executors.put("python", new PythonExecutorFactory().createBot());
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }
    public void startTimeout(long timeout, Bot bot){
        this.bot = bot;
        this.start();

        try {
            this.join(timeout);    //  最多等待多长时间，之后执行下面代码
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }

    //同一个类名只编译一次，因此在类名后面加一个随机字符串
    @Override
    public void run(){
        //ljp修改：下面通过传来的语言类型决定使用哪种机器人执行器
        BotExecutorInterface executor = executors.get(bot.getLanguage().toLowerCase());
        if (executor == null) {
            throw new RuntimeException("不支持这种语言: " + bot.getLanguage());
        }

        Integer direction = executor.nextMove(bot.getBotCode(), bot.getInput());

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", bot.getUserId().toString());
        data.add("botId", bot.getBotId().toString());
        data.add("direction", direction.toString());
        String resp = restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
        System.out.println(resp);
        System.out.println(direction);
    }
}
