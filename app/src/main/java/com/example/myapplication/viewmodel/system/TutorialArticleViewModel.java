package com.example.myapplication.viewmodel.system;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.adapter.TutorialArticleAdapter;
import com.example.myapplication.model.TutorialArticle;

import java.util.ArrayList;
import java.util.List;

public class TutorialArticleViewModel extends ViewModel {
    private final SystemRepository repository;
    private final MutableLiveData<List<TutorialArticle>> tutorialArticlesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private TutorialArticleAdapter adapter;
    private boolean hasMore = true;
    private final int currentPage = 0;
    public TutorialArticleViewModel(SystemRepository repository) {
        this.repository = repository;
        // 初始化时加载教程数据
        loadTutorialArticles();
    }

    //=== 核心方法 ===//
    public void refreshTutorialArticles() {
        hasMore = true;
        loadTutorialArticles();
        Log.d("TutorialVM", "触发刷新教程数据");
    }

    private void loadTutorialArticles() {
        if (Boolean.TRUE.equals(isLoading.getValue())) return;

        isLoading.setValue(true);
        Log.d("TutorialVM", "开始请求教程数据");

        repository.getTutorialArticleList(new Repository.Callback<List<TutorialArticle>>() {
            @Override
            public void onSuccess(List<TutorialArticle> data) {
                handleDataResponse(data);
                Log.d("TutorialVM", "请求成功 | 数据量=" + data.size());
            }

            @Override
            public void onFailure(Throwable t) {
                handleError(t);
                Log.e("TutorialVM", "请求失败: " + t.getMessage());
            }
        });
    }

    // 收藏教程文章
    // 取消收藏教程文章


    //=== 数据处理 ===//
    private void handleDataResponse(List<TutorialArticle> newData) {
        isLoading.postValue(false);
        hasMore = !newData.isEmpty();
        List<TutorialArticle> currentList = tutorialArticlesLiveData.getValue() != null ?
                new ArrayList<>(tutorialArticlesLiveData.getValue()) : new ArrayList<>();

        if (currentPage == 0) {
            currentList = newData; // 刷新时完全替换
        } else {
            currentList.addAll(newData); // 加载更多时追加
        }

        tutorialArticlesLiveData.postValue(currentList);

        // 如果有adapter，通知数据变化
    }

    private void handleError(Throwable t) {
        isLoading.postValue(false);
        errorMsg.postValue(t.getMessage());
    }

    // 更新文章收藏状态


    //=== 暴露的LiveData ===//
    public LiveData<List<TutorialArticle>> getTutorialArticles() {
        return tutorialArticlesLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    public boolean hasMore() {
        return hasMore;
    }

    //=== Adapter相关 ===//
    public void setAdapter(TutorialArticleAdapter adapter) {
        this.adapter = adapter;
    }

    public TutorialArticleAdapter getAdapter() {
        return adapter;
    }
}