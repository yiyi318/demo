package com.example.myapplication.model;

import java.util.List;

// RankResponse.java
public class RankResponse {
    private int errorCode;
    private String errorMsg;
    private RankData data;

    // Getters
    public boolean isSuccess() {
        return errorCode == 0;
    }
    public List<CoinInfo> getRankList() {
        return data != null ? data.datas : null;
    }

    public class RankData {
        List<CoinInfo> datas;
        int curPage;
        int total;
        // 其他分页字段...
    }
}

// RankData.java
