package com.example.myapplication.model;

import com.google.gson.JsonObject;

// User.java
public class User {
    private final String username;
    private final String nickname;
    private final String email;
    private final int userId;
    private final int level;
    private final String rank;
    private final int coinCount;
    private final String cookie; // 登录凭证

    // 构造函数
    public User(String username, String nickname, String email, int userId,
                int level, String rank, int coinCount, String cookie) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.userId = userId;
        this.level = level;
        this.rank = rank;
        this.coinCount = coinCount;
        this.cookie = cookie;
    }

    // Getters
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public int getUserId() { return userId; }
    public int getLevel() { return level; }
    public String getRank() { return rank; }
    public int getCoinCount() { return coinCount; }
    public String getCookie() { return cookie; }

    // 可选：从JSON解析为User对象
    public static User fromJson(JsonObject json) {
        JsonObject coinInfo = json.getAsJsonObject("coinInfo");
        JsonObject userInfo = json.getAsJsonObject("userInfo");

        return new User(
                userInfo.get("username").getAsString(),
                userInfo.get("nickname").getAsString(),
                userInfo.get("email").getAsString(),
                userInfo.get("id").getAsInt(),
                coinInfo.get("level").getAsInt(),
                coinInfo.get("rank").getAsString(),
                coinInfo.get("coinCount").getAsInt(),
                "" // cookie需要从登录接口单独获取
        );
    }
}