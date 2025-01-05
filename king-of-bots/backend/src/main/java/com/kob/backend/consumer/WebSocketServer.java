package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.handler.MessageHandler;
import com.kob.backend.consumer.handler.MoveHandler;
import com.kob.backend.consumer.handler.StartMatchHandler;
import com.kob.backend.consumer.handler.StopMatchHandler;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Game;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketServer {
    // zzy: 静态变量存储客户端ID和WebSocketServer实例的对应关系
    public static final ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    private MessageHandler chain; // zzy: 责任链的起点
    private Session session;      // zzy: 当前WebSocket的会话
    private User user;            // zzy: 当前用户信息
    public Game game;             // zzy: 当前用户所在的游戏

    // Spring Bean注入
    public static UserMapper userMapper;
    public static RecordMapper recordMapper;
    public static RestTemplate restTemplate;
    public static BotMapper botMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }

    // zzy: 初始化责任链
    public WebSocketServer() {
        initChain();
    }

    private void initChain() {
        MessageHandler startMatchHandler = new StartMatchHandler();
        MessageHandler stopMatchHandler = new StopMatchHandler();
        MessageHandler moveHandler = new MoveHandler();

        startMatchHandler.setNext(stopMatchHandler);
        stopMatchHandler.setNext(moveHandler);

        this.chain = startMatchHandler;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        System.out.println("连接了一个客户端");
        int userId = -1;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userId == -1) {
            session.close();
            return;
        }

        this.session = session;
        this.user = userMapper.selectById(userId);

        if (this.user != null) {
            users.put(userId, this);
        } else {
            session.close();
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("断开了一个客户端的连接");
        if (user != null) {
            users.remove(user.getId());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("收到来自客户端的信息: " + message);
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");

        // zzy: 使用责任链处理事件
        chain.handle(event, data, this);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // zzy: 游戏开始逻辑
    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId);
        User b = userMapper.selectById(bId);

        Game game = new Game(13, 13, 20, a.getId(), null, b.getId(), null);
        game.createGameMap();

        if (users.get(a.getId()) != null) {
            users.get(a.getId()).game = game;
        }
        if (users.get(b.getId()) != null) {
            users.get(b.getId()).game = game;
        }
        game.start();

        JSONObject resp = new JSONObject();
        resp.put("a_id", game.getPlayerA().getId());
        resp.put("b_id", game.getPlayerB().getId());
        resp.put("map", game.getGameMap());

        if (users.get(a.getId()) != null) {
            users.get(a.getId()).sendMessage(resp.toJSONString());
        }
        if (users.get(b.getId()) != null) {
            users.get(b.getId()).sendMessage(resp.toJSONString());
        }
    }

    // zzy: Getter方法，提供给处理器使用
    public User getUser() {
        return user;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getAddPlayerUrl() {
        String addPlayerUrl = "http://localhost:3001/player/add/";
        return addPlayerUrl;
    }

    public String getRemovePlayerUrl() {
        String removePlayerUrl = "http://localhost:3001/player/remove/";
        return removePlayerUrl;
    }

    public Game getGame() {
        return game;
    }
}
