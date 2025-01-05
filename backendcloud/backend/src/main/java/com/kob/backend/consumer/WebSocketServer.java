package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kob.backend.constants.Constants.*;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    //匹配池，线程安全
    //final private static CopyOnWriteArraySet<User> matchPool = new CopyOnWriteArraySet<>();

    //线程安全的静态变量存储客户端id和websockeserver的对应关系
    final public static ConcurrentHashMap<Pair<Integer, Integer>, WebSocketServer> users = new ConcurrentHashMap<>();
    private static AtomicInteger total = new AtomicInteger(0);

    private User user = null;
    private Integer oldValue = -1;    // selectedBotId改变，oldValue不变
    private Integer selectedBotId = -1;
    private Session session = null;
    public Game game = null;
    private final String addPlayerUrl = "http://localhost:3001/player/add/";
    private final String removePlayerUrl = "http://localhost:3001/player/remove/";

    public static UserMapper userMapper;
    public static RecordMapper recordMapper;
    public static RestTemplate restTemplate;
    private static BotMapper botMapper;
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

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        System.out.println("连接了一个客户端");
        // 建立连接
        int userId = -1;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(userId == -1) {
            session.close();
        } else {
            this.session = session;
            this.user = userMapper.selectById(userId);
            selectedBotId = total.decrementAndGet();
            oldValue = selectedBotId;
            users.put(Pair.of(user.getId(), selectedBotId), this);
            JSONObject resp = new JSONObject();    //  连接好之后同步botId
            resp.put("event", "sync-botId");
            resp.put("botId", selectedBotId);
            sendMessage(resp.toJSONString());
        }
    }
    @OnClose
    public void onClose() {
        System.out.println("断开了一个客户端的连接");
        // 关闭链接
        if(user != null) {
            users.remove(Pair.of(user.getId(), selectedBotId));
            //matchPool.remove(user);
        }
    }
    @OnMessage
    public void onMessage(String message, Session session) {    //  onMessage，一般用来做分类，根据event的内容，转给不同方法处理
        System.out.println("收到来自客户端的信息");    // 客户端向服务器端发
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");

        if("start-match".equals(event)) {
            startMatch(data.getInteger("bot_id"));
        } else if("stop-match".equals(event)) {
            stopMatch();
        } else if("move".equals(event)) {
            move(data.getInteger("d"));
        } else if("update-selected-bot".equals(event)){
            Integer oldBot = Integer.parseInt(data.getString("old_bot"));
            Integer newBot = Integer.parseInt(data.getString("new_bot"));
            updateSelectedBot(oldBot, newBot);
        }
    }
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    //在机器人对战时，人的输入不接收
    private void move(int d) {
        if(game.getPlayerA().getId().equals(user.getId())) {
            if(game.getPlayerA().getBotId() <= -1)game.setNextStepA(d);
        } else if(game.getPlayerB().getId().equals(user.getId())) {
            if(game.getPlayerB().getBotId() <= -1)game.setNextStepB(d);
        }
    }
    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId);User b = userMapper.selectById(bId);
        Bot aBot = botMapper.selectById(aBotId);Bot bBot = botMapper.selectById(bBotId);

        Game game = new Game(ROWS, COLS, INNER_WALLS_COUNT, a.getId(), aBot, b.getId(), bBot);
        game.createGameMap();
        if(users.get(Pair.of(a.getId(), aBotId)) != null){     //在玩家匹配时意外断开，但是玩家仍在匹配池中的情况，此时WebSocketServer是空，会空指针
            users.get(Pair.of(a.getId(), aBotId)).game = game;
        }
        if(users.get(Pair.of(b.getId(), bBotId)) != null){
            users.get(Pair.of(b.getId(), bBotId)).game = game;
        }
        game.start();

        JSONObject resp = new JSONObject();

        resp.put("a_id", game.getPlayerA().getId());
        resp.put("a_sx", game.getPlayerA().getSx());
        resp.put("a_sy", game.getPlayerA().getSy());

        resp.put("b_id", game.getPlayerB().getId());
        resp.put("b_sx", game.getPlayerB().getSx());
        resp.put("b_sy", game.getPlayerB().getSy());

        resp.put("map", game.getGameMap());

        JSONObject respA = new JSONObject();
        JSONObject respB = new JSONObject();
        respA.put("opponent_name", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("event", "match-found");
        respA.put("me", "A");
        respA.put("game", resp);

        respB.put("opponent_name", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("event", "match-found");
        respB.put("me", "B");
        respB.put("game", resp);

        if(users.get(Pair.of(a.getId(), aBotId)) != null){
            users.get(Pair.of(a.getId(), aBotId)).sendMessage(respA.toJSONString());
            System.out.println("a" + users.get(Pair.of(a.getId(), aBotId)).session.getId());
            System.out.println("给a发送成功");
            System.out.println(users.size());
            System.out.println(users.get(Pair.of(b.getId(), bBotId)));
        }
        if(users.get(Pair.of(b.getId(), bBotId)) != null){
            users.get(Pair.of(b.getId(), bBotId)).sendMessage(respB.toJSONString());
            System.out.println("b" + users.get(Pair.of(b.getId(), bBotId)).session.getId());
            System.out.println("给b发送成功");
            System.out.println(users.size());
        }

    }
    //先点的左下角后点的右上角
    private void startMatch(Integer botId){
        System.out.println("调试信息：开始匹配");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", user.getId().toString());
        data.add("rating", user.getRating().toString());
        data.add("botId", botId.toString());
        String resp = restTemplate.postForObject(addPlayerUrl, data, String.class);
        System.out.println(resp);
//        matchPool.add(user);
//        while(matchPool.size() >= 2){
//            Iterator<User> iterator = matchPool.iterator();     //  先进去的是a，左下
//            User a = iterator.next(), b = iterator.next();
//            matchPool.remove(a);
//            matchPool.remove(b);
//
//
//        }
    }
    private void stopMatch(){
        System.out.println("调试信息：停止匹配");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
        //matchPool.remove(user);
    }
    private void updateSelectedBot(Integer oldBot, Integer newBot){         //  用户如果又改成亲自出战还要同步botId
        if(oldBot > 0 && newBot == -1){      //  选了机器人又改回亲自出马
            selectedBotId = oldValue;
            newBot = oldValue;
            JSONObject resp = new JSONObject();    //  连接好之后同步botId
            resp.put("event", "sync-botId");
            resp.put("botId", oldValue);
            sendMessage(resp.toJSONString());
        } else if(oldBot < 0 && newBot > 0){   // 改成机器人
            selectedBotId = newBot;
        } else if(oldBot < 0 && newBot == -1){  // 又点了一次亲自出马的
            selectedBotId = oldValue;
            newBot = oldValue;
            JSONObject resp = new JSONObject();    //  连接好之后同步botId
            resp.put("event", "sync-botId");
            resp.put("botId", oldValue);
            sendMessage(resp.toJSONString());
        } else if(oldBot > 0 && newBot > 0){    // 选机器人又改了另一个机器人
            selectedBotId = newBot;
        }
        Pair<Integer, Integer> oldKey = Pair.of(user.getId(), oldBot);
        Pair<Integer, Integer> newKey = Pair.of(user.getId(), newBot);
        users.remove(oldKey);
        users.put(newKey, this);
    }
    public void sendMessage(String message) {     //  服务器端向客户端发送
        synchronized (session) {
            try{
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}