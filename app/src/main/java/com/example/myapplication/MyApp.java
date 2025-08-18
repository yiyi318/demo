package com.example.myapplication;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;
//
public class MyApp extends Application {
//    时机正确：在任何Activity创建之前设置主题
    @Override
    public void onCreate() {
        initTheme();
        super.onCreate();
    }

    /**
     * 初始化主题设置，根据 SharedPreferences 中的配置设置夜间模式。
     */
    private void initTheme() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int savedNightMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedNightMode);
    }
}
