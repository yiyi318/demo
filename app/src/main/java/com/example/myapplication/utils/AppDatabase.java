package com.example.myapplication.utils;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.model.History;

// 数据库版本号，升级时需递增
@Database(entities = {History.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // 单例模式，避免重复创建数据库实例
    private static volatile AppDatabase INSTANCE;

    // 提供 DAO 实例
    public abstract HistoryDao historyDao();

    // 获取数据库实例
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // 创建数据库（name 为数据库文件名）
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"
                            )
                            .allowMainThreadQueries() // 允许在主线程操作（仅测试用，正式环境需移除）
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}