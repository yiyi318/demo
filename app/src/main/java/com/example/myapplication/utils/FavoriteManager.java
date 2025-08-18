package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;
import java.util.stream.Collectors;

// FavoriteManager.java
public class FavoriteManager {
    private static final String PREFS_NAME = "user_favorites";
    private final SharedPreferences prefs;

    public FavoriteManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 添加收藏
    public void addFavorite(String articleId) {
        prefs.edit().putBoolean("fav_" + articleId, true).apply();
    }

    // 移除收藏
    public void removeFavorite(String articleId) {
        prefs.edit().remove("fav_" + articleId).apply();
    }

    // 获取所有收藏的ID
    public Set<String> getAllFavorites() {
        return prefs.getAll().keySet()
                .stream()
                .filter(key -> key.startsWith("fav_"))
                .map(key -> key.substring(4)) // 移除 "fav_" 前缀
                .collect(Collectors.toSet());
    }
}