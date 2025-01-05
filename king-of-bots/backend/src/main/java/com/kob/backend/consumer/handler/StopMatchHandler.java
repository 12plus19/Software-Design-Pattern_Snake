package com.kob.backend.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import org.springframework.util.LinkedMultiValueMap;

// zzy: 处理“停止匹配”事件
public class StopMatchHandler extends MessageHandler {
    @Override
    protected boolean canHandle(String event) {
        return "stop-match".equals(event);
    }

    @Override
    protected void process(JSONObject data, WebSocketServer server) {
        System.out.println("调试信息：停止匹配");
        LinkedMultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
        requestData.add("userId", server.getUser().getId().toString());
        server.getRestTemplate().postForObject(server.getRemovePlayerUrl(), requestData, String.class);
    }
}
