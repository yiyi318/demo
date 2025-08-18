package com.example.myapplication.viewmodel.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.model.History;
import com.example.myapplication.utils.AppDatabase;
import com.example.myapplication.utils.HistoryDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryViewModel extends AndroidViewModel {
    private final HistoryDao historyDao;
    private final LiveData<List<History>> allHistories;
    private final ExecutorService executorService;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        historyDao = db.historyDao();
        allHistories = historyDao.getAllHistories();
        executorService = Executors.newSingleThreadExecutor(); // 创建线程池
    }

    // 获取所有历史记录
    public LiveData<List<History>> getAllHistories() {
        return allHistories;
    }



    // 删除历史记录
    public void deleteHistory(History history) {
        executorService.execute(() -> {
            historyDao.delete(history);
        });

    }

    // 添加/更新历史记录
    public void addOrUpdateHistory(History newHistory) {
        executorService.execute(() -> {
            // 先查询是否已有该链接的记录
            History existingHistory = historyDao.findByLink(newHistory.getLink());
            if (existingHistory != null) {
                // 已存在：更新访问时间和其他可能变化的字段
                existingHistory.setVisitTime(System.currentTimeMillis());
                // 如果有其他字段需要更新（如标题），也可以在这里设置
                historyDao.update(existingHistory); // 需要在 DAO 中添加 update 方法
            } else {
                // 不存在：插入新记录
                historyDao.insert(newHistory);
            }
            // 限制历史记录数量
            limitHistoryCount(50);
        });

    }

    private void limitHistoryCount(int maxCount) {
        // 查询超过数量的旧记录并删除
        int totalCount = historyDao.getHistoryCount();
        if (totalCount > maxCount) {
            // 计算需要删除的数量
            int deleteCount = totalCount - maxCount;
            // 获取最旧的deleteCount条记录
            List<History> oldestHistories = historyDao.getOldestHistories(deleteCount);
            if (!oldestHistories.isEmpty()) {
                historyDao.deleteHistories(oldestHistories);
            }
        }
    }


    // 清空所有历史记录
    public void clearAllHistories() {
        executorService.execute(() -> {
            historyDao.clearAll();
        });
    }
}