package com.example.myapplication.viewmodel.system;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.model.Article;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;

public class SquareViewModel extends ViewModel {
    private final SystemRepository repository;
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private int currentPage = 0;
    private boolean hasMore = true;

    public SquareViewModel(SystemRepository repository) {
        this.repository = repository;
    }


    public void refreshArticles() {
        if (isLoading.getValue() == Boolean.TRUE) return;
        currentPage = 0;
        hasMore = true;
        loadArticles();
    }

    public void loadMoreArticles() {
        Log.d("square", "loadMoreArticles: ");

        if (!hasMore || isLoading.getValue() == Boolean.TRUE) return;
        currentPage++;
        loadArticles();
    }


    private void loadArticles() {
        if (isLoading.getValue() == Boolean.TRUE) return; // 防抖
        isLoading.setValue(true);
        repository.getSquareArticles(currentPage, new Repository.Callback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> data) {
                isLoading.setValue(false);
                hasMore = !data.isEmpty();

                if (currentPage == 0) {
                    articles.setValue(data);
                } else {
                    List<Article> current = articles.getValue() != null ?
                            articles.getValue() : new ArrayList<>();
                    current.addAll(data);
                    articles.setValue(current);
                }

                Log.d("VM_STATE", "加载完成 | 页码:" + currentPage +
                        " | 是否有更多:" + hasMore + " | 数据量:" + data.size());
            }

            @Override
            public void onFailure(Throwable t) {
                isLoading.setValue(false);
                errorMsg.setValue(t.getMessage());
                if (currentPage > 0) currentPage--;
                Log.e("VM_ERROR", "加载失败: " + t.getMessage());

                String errorMsg = "请求失败";
                if (t instanceof JsonParseException) {
                    errorMsg = "数据解析错误: " + t.getMessage();
                }
                isLoading.postValue(false);
//                this.errorMsg.postValue(errorMsg);
            }
        });
    }

    // Getters...
    public LiveData<List<Article>> getArticles() { return articles; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMsg() { return errorMsg; }
    public boolean hasMore() { return hasMore; }
}