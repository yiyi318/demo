package com.example.myapplication.model;

// CoinInfo.java
public class CoinInfo {
    private int coinCount;
    private int level;
    private String nickname;
    private String rank;
    private int userId;
    private String username;

    // Getters
    public int getCoinCount() { return coinCount; }
    public int getLevel() { return level; }
    public String getNickname() { return nickname; }
    public String getRank() { return rank; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }

    // 可添加toString()方便调试
    @Override
    public String toString() {
        return String.format("Lv.%d 排名%s | 积分:%d", level, rank, coinCount);
    }
}
