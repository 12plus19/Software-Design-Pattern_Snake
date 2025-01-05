package com.kob.backend.consumer.handler;

import com.alibaba.fastjson.JSONObject;

// zzy: 处理“移动”事件
public class MoveHandler extends MessageHandler {
    private final Game game; // 引用 Game 对象

    public MoveHandler() {
        this.game = game;
    }

    @Override
    public void handle(JSONObject message) {
        // 提取移动方向
        int direction = message.getInteger("d");

        // 判断玩家是 A 还是 B，并设置下一步操作
        if (game.getPlayerA().getId().equals(user.getId())) {
            if (game.getPlayerA().getBotId() == -1) {
                game.setNextStepA(direction); // 设置玩家 A 的下一步操作
            }
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if (game.getPlayerB().getBotId() == -1) {
                game.setNextStepB(direction); // 设置玩家 B 的下一步操作
            }
        }
    }
}

