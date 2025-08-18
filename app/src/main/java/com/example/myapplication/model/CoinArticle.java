package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class CoinArticle {
    // 积分总数
    @SerializedName("coinCount")
    private int coinCount;

    // 操作时间（时间戳，毫秒）
    @SerializedName("date")
    private long date;

    // 操作描述
    @SerializedName("desc")
    private String desc;

    // 记录ID
    @SerializedName("id")
    private int id;

    // 操作原因（如"签到"、"分享文章"）
    @SerializedName("reason")
    private String reason;

    // 操作类型（1：签到，3：分享文章等）
    @SerializedName("type")
    private int type;

    // 用户ID
    @SerializedName("userId")
    private int userId;

    // 用户名
    @SerializedName("userName")
    private String userName;

    // 无参构造方法（序列化/反序列化需要）
    public CoinArticle() {
    }

    // 全参构造方法（可选，便于创建对象）
    public CoinArticle(int coinCount, long date, String desc, int id, String reason, int type, int userId, String userName) {
        this.coinCount = coinCount;
        this.date = date;
        this.desc = desc;
        this.id = id;
        this.reason = reason;
        this.type = type;
        this.userId = userId;
        this.userName = userName;
    }

    // Getter 和 Setter 方法
    public int getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(int coinCount) {
        this.coinCount = coinCount;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}