package com.kob.backend.constants;

public class Constants {
    // zzy: WebSocket事件类型
    public static final String EVENT_START_MATCH = "start-match";
    public static final String EVENT_STOP_MATCH = "stop-match";
    public static final String EVENT_MOVE = "move";

    // zzy: 游戏配置
    public static final int GAME_ROWS = 13;
    public static final int GAME_COLS = 13;
    public static final int INNER_WALLS_COUNT = 20;

    // zzy: 匹配相关URL
    public static final String ADD_PLAYER_URL = "http://localhost:3001/player/add/";
    public static final String REMOVE_PLAYER_URL = "http://localhost:3001/player/remove/";

    // 其他可能需要的常量
}
