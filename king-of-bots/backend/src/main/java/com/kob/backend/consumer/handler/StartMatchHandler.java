package com.kob.backend.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import org.springframework.util.LinkedMultiValueMap;

// zzy: 处理“开始匹配”事件
public class StartMatchHandler extends MessageHandler {
    @Override
    protected boolean canHandle(String event) {
        return "start-match".equals(event);
    }

    @Override
    protected void process(JSONObject data, WebSocketServer server) {
        Integer botId = data.getInteger("bot_id");
        System.out.println("调试信息：开始匹配");
        LinkedMultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
        requestData.add("userId", server.getUser().getId().toString());
        requestData.add("rating", server.getUser().getRating().toString());
        requestData.add("botId", botId.toString());
        String response = server.getRestTemplate().postForObject(server.getAddPlayerUrl(), requestData, String.class);
        System.out.println(response);
    }
}
