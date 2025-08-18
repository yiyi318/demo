package com.example.myapplication.utils;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.History;

import java.util.List;

@Dao
public interface HistoryDao {
    // 插入一条历史记录（如果链接相同可替换，避免重复）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(History history);

    // 删除一条历史记录
    @Delete
    void delete(History history);

    // 清空所有历史记录
    @Query("DELETE FROM history_records")
    void clearAll();

    // 查询所有历史记录（按访问时间倒序，最新的在前面）
    @Query("SELECT * FROM history_records ORDER BY visitTime DESC")
    LiveData<List<History>> getAllHistories();

    // 可选：根据链接查询是否已存在（避免重复添加）
    @Query("SELECT * FROM history_records WHERE link = :link LIMIT 1")
    History findByLink(String link);

    @Update
    void update(History history);

    @Query("SELECT * FROM history_records ORDER BY visitTime ASC LIMIT :count")
    List<History> getOldestHistories(int count);

    @Delete
    void deleteHistories(List<History> histories);

    @Query("SELECT COUNT(*) FROM history_records")
    int getHistoryCount();
}