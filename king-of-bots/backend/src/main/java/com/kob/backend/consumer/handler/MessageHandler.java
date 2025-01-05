package com.kob.backend.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;

// zzy: 定义抽象事件处理器
public abstract class MessageHandler {
    protected MessageHandler next;

    public void setNext(MessageHandler next) {
        this.next = next;
    }

    public void handle(String event, JSONObject data, WebSocketServer server) {
        if (canHandle(event)) {
            process(data, server);
        } else if (next != null) {
            next.handle(event, data, server);
        }
    }

    // zzy: 判断是否能处理当前事件
    protected abstract boolean canHandle(String event);

    // zzy: 处理当前事件的具体逻辑
    protected abstract void process(JSONObject data, WebSocketServer server);
}
