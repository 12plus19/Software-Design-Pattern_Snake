package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class GameFacade {
    private final Game game;

    // zzy修改：初始化GameFacade并绑定Game对象
    public GameFacade(Game game) {
        this.game = game;
    }

    // zzy修改：初始化游戏地图
    public void initializeGame() {
        game.createGameMap();
    }

    // zzy修改：开始游戏主线程
    public void startGame() {
        game.start();
    }

    // zzy修改：处理玩家下一步操作
    public void handleNextStep(Integer stepA, Integer stepB) {
        game.setNextStepA(stepA);
        game.setNextStepB(stepB);
    }

    // zzy修改：发送比赛结果
    public void sendResult() {
        JSONObject result = new JSONObject();
        result.put("event", "result");
        result.put("loser", game.getLoser());
        game.sendAllMessage(result.toJSONString());
    }

    // zzy修改：发送玩家移动信息
    public void sendMove() {
        game.sendMove();
    }

    // zzy修改：判断比赛状态
    public void judge() {
        game.judge();
    }
}
