package com.kob.matchingsystem.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zzy：BaseMatchingPool是原始的匹配池功能实现类，负责添加玩家、移除玩家、增加等待时间、
 * 尝试匹配玩家并发送匹配结果。
 */
@Component
public class BaseMatchingPool implements MatchingPoolDecorator {
    private static List<Player> players = new ArrayList<>();  // zzy：存储所有玩家的列表
    private final ReentrantLock lock = new ReentrantLock(); // zzy：用于线程安全的锁
    private static RestTemplate restTemplate; // zzy：用于发送请求的RestTemplate实例
    private final static String startGameUrl = "http://localhost:3000/pk/start/game/"; // zzy：游戏开始的URL

    // zzy：自动注入RestTemplate
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        BaseMatchingPool.restTemplate = restTemplate;
    }

    // zzy：添加玩家到匹配池
    @Override
    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        lock.lock(); // zzy：获取锁
        try {
            players.add(new Player(userId, rating, botId, 0)); // zzy：将新玩家添加到列表
        } finally {
            lock.unlock(); // zzy：释放锁
        }
    }

    // zzy：从匹配池中移除玩家
    @Override
    public void removePlayer(Integer userId) {
        lock.lock(); // zzy：获取锁
        try {
            List<Player> newPlayers = new ArrayList<>();
            for (Player player : players) {
                if (!player.getUserId().equals(userId)) { // zzy：如果玩家ID不匹配，则保留
                    newPlayers.add(player);
                }
            }
            players = newPlayers; // zzy：更新玩家列表
        } finally {
            lock.unlock(); // zzy：释放锁
        }
    }

    // zzy：增加所有玩家的等待时间
    @Override
    public void increasingWaitingTime() {
        lock.lock(); // zzy：获取锁
        try {
            for (Player player : players) {
                player.setWaitingTime(player.getWaitingTime() + 1); // zzy：每个玩家等待时间加1
            }
        } finally {
            lock.unlock(); // zzy：释放锁
        }
    }

    // zzy：尝试匹配所有玩家
    @Override
    public void matchPlayers() {
        lock.lock(); // zzy：获取锁
        try {
            boolean[] used = new boolean[players.size()]; // zzy：用于标记已经匹配的玩家
            for (int i = 0; i < players.size(); i++) {
                if (used[i]) continue; // zzy：如果当前玩家已匹配，跳过
                for (int j = i + 1; j < players.size(); j++) {
                    if (used[j]) continue; // zzy：如果对方玩家已匹配，跳过
                    Player a = players.get(i), b = players.get(j); // zzy：获取待匹配玩家
                    if (checkMatched(a, b)) { // zzy：检查是否匹配
                        used[i] = used[j] = true; // zzy：标记玩家已匹配
                        sendResult(a, b); // zzy：发送匹配结果
                        break; // zzy：匹配成功，跳出内层循环
                    }
                }
            }
            // zzy：移除所有已匹配的玩家
            List<Player> newPlayers = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                if (!used[i]) {
                    newPlayers.add(players.get(i));
                }
            }
            players = newPlayers; // zzy：更新玩家列表
        } finally {
            lock.unlock(); // zzy：释放锁
        }
    }

    // zzy：判断两个玩家是否匹配
    private boolean checkMatched(Player a, Player b) {
        int ratingDelta = Math.abs(a.getRating() - b.getRating()); // zzy：计算玩家评分差距
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime()); // zzy：选择最小的等待时间
        return waitingTime * 10 >= ratingDelta; // zzy：如果等待时间足够，认为玩家匹配
    }

    // zzy：发送匹配结果
    private void sendResult(Player a, Player b) {
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("aId", a.getUserId().toString());
        data.add("aBotId", a.getBotId().toString());
        data.add("bId", b.getUserId().toString());
        data.add("bBotId", b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class); // zzy：发送匹配结果
    }

    // zzy：启动匹配池的执行
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000); // zzy：每秒钟执行一次
                increasingWaitingTime(); // zzy：增加所有玩家的等待时间
                matchPlayers(); // zzy：尝试匹配玩家
            } catch (InterruptedException e) {
                e.printStackTrace();
                break; // zzy：如果线程被中断，退出循环
            }
        }
    }
}
