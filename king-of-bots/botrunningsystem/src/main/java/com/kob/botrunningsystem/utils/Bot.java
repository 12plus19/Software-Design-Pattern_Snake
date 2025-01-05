package com.kob.botrunningsystem.utils;

/**
 * zzy: Bot类，封装用户ID、代码和输入信息
 */
public class Bot {
    private Integer userId; // zzy: 用户ID
    private String botCode; // zzy: Bot代码
    private String input;   // zzy: 输入信息

    // zzy: 构造方法
    public Bot(Integer userId, String botCode, String input) {
        this.userId = userId;
        this.botCode = botCode;
        this.input = input;
    }

    // zzy: Getter和Setter方法
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getBotCode() {
        return botCode;
    }

    public void setBotCode(String botCode) {
        this.botCode = botCode;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
