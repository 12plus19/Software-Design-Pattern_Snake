package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.Game;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.pk.ReceiveBotMoveService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {

    @Override
    public String receiveBotMove(Integer userId, Integer botId, Integer direction) {
        System.out.println("receive bot move success");
        if(WebSocketServer.users.get(Pair.of(userId, botId)) != null){
            Game game = WebSocketServer.users.get(Pair.of(userId, botId)).game;
            if(game != null){
                if(Objects.equals(game.getPlayerA().getId(), userId)){
                    game.setNextStepA(direction);
                } else if(Objects.equals(game.getPlayerB().getId(), userId)){
                    game.setNextStepB(direction);
                }
            }
        }
        return "receive bot move success";
    }
}
