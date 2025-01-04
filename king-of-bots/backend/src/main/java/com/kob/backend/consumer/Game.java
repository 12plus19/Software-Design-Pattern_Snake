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

public class Game extends Thread {
    private final Integer rows;
    private final Integer cols;
    private final Integer innerWallsCount;
    @Getter
    private final int[][] gameMap;
    private final static int[] dx = {-1, 0, 1, 0};
    private final static int[] dy = {0, 1, 0, -1};
    @Getter
    private final Player playerA, playerB;
    private Integer nextStepA = null;
    private Integer nextStepB = null;
    private final ReentrantLock lock = new ReentrantLock();
    private String status = "playing";  // playing finished
    @Getter
    private String loser = ""; // all, A, B

    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";

    public Game(Integer rows, Integer cols, Integer innerWallsCount, Integer idA, Bot aBot, Integer idB, Bot bBot) {
        this.rows = rows;
        this.cols = cols;
        this.innerWallsCount = innerWallsCount;
        gameMap = new int[rows][cols];

        Integer aBotId = -1, bBotId = -1;
        String aBotCode = "", bBotCode = "";
        if (aBot != null) {
            aBotId = aBot.getId();
            aBotCode = aBot.getCode();
        }
        if (bBot != null) {
            bBotId = bBot.getId();
            bBotCode = bBot.getCode();
        }

        playerA = new Player(idA, aBotId, aBotCode, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, bBotId, bBotCode, 1, cols - 2, new ArrayList<>());
    }

    // zzy修改：将地图生成与验证逻辑封装
    public void createGameMap() {
        for (int i = 0; i < 1000; i++) {
            if (createWalls()) {
                break;
            }
        }
    }

    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try{
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }
    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try{
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    private boolean createWalls() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gameMap[i][j] = 0;
            }
        }
        for (int r = 0; r < rows; r++) {
            gameMap[r][0] = gameMap[r][cols - 1] = 1;
        }
        for (int c = 0; c < cols; c++) {
            gameMap[0][c] = gameMap[rows - 1][c] = 1;
        }
        Random rand = new Random();
        for (int i = 0; i < innerWallsCount / 2; i++) {
            for (int j = 0; j < 1000; j++) {
                int r = rand.nextInt(rows);
                int c = rand.nextInt(cols);
                if (gameMap[r][c] == 1 || gameMap[rows - 1 - r][cols - 1 - c] == 1) {
                    continue;
                }
                if (r == rows - 2 && c == 1 || r == 1 && c == cols - 2) {
                    continue;
                }
                gameMap[r][c] = gameMap[rows - 1 - r][cols - 1 - c] = 1;
                break;
            }
        }
        return checkConnect(rows - 2, 1, 1, cols - 2);
    }

    private boolean checkConnect(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        gameMap[sx][sy] = 1;
        for (int i = 0; i < 4; i++) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < rows && y >= 0 && y < cols && gameMap[x][y] == 0) {
                if (checkConnect(x, y, tx, ty)) {
                    gameMap[sx][sy] = 0;
                    return true;
                }
            }
        }
        gameMap[sx][sy] = 0;
        return false;
    }

    // zzy修改：发送消息逻辑移动至外观类调用
    protected void sendAllMessage(String message) {
        if (WebSocketServer.users.get(playerA.getId()) != null) {
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        }
        if (WebSocketServer.users.get(playerB.getId()) != null) {
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
        }
    }

    // zzy修改：玩家移动逻辑封装到外观类中
    protected void sendMove() {
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_move", nextStepA);
            resp.put("b_move", nextStepB);
            nextStepA = null;
            nextStepB = null;
            sendAllMessage(resp.toJSONString());
        } finally {
            lock.unlock();
        }
    }

    // zzy修改：判断比赛状态
    protected void judge() {
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = checkValid(cellsA, cellsB);
        boolean validB = checkValid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";
            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    private boolean checkValid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);
        if (gameMap[cell.getX()][cell.getY()] == 1) {
            return false;
        }
        for (int i = 0; i < n - 1; i++) {
            if (cellsA.get(i).getX() == cell.getX() && cellsA.get(i).getY() == cell.getY()) {
                return false;
            }
        }
        for (int i = 0; i < n - 1; i++) {
            if (cellsB.get(i).getX() == cell.getX() && cellsB.get(i).getY() == cell.getY()) {
                return false;
            }
        }
        return true;
    }
}
