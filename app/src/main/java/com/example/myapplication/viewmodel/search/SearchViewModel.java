package com.example.myapplication.viewmodel.search;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.Article;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    private final Repository repository;
    private final MutableLiveData<List<Article>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private int currentPage = 0;
    private boolean hasMore = true;
    private String currentKeywordId; // 当前搜索关键词ID

    public SearchViewModel(Repository repository) {
        this.repository = repository;
    }

    //=== 核心方法 ===//
    public void newSearch(String keywordId) {
        currentPage = 0;
        currentKeywordId = keywordId;
        hasMore = true;
        loadSearchResults();
        Log.d("SearchVM", "新搜索 | 关键词ID=" + keywordId);
    }

    public void loadMoreResults() {
        if (hasMore && !Boolean.TRUE.equals(isLoading.getValue())) {
            currentPage++;
            loadSearchResults();
            Log.d("SearchVM", "加载更多 | 页码=" + currentPage);
        }
    }



    private void loadSearchResults() {
        if (Boolean.TRUE.equals(isLoading.getValue())) return;

        isLoading.setValue(true);
        Log.d("SearchVM", "开始搜索请求 | 页码=" + currentPage + " 关键词ID=" + currentKeywordId);

        repository.searchArticles(currentPage, currentKeywordId, new Repository.Callback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> data) {
                handleDataResponse(data);
                Log.d("SearchVM", "搜索成功 | 结果数=" + data.size());
            }

            @Override
            public void onFailure(Throwable t) {
                handleError(t);
                Log.e("SearchVM", "搜索失败: " + t.getMessage());
            }
        });
    }

    //=== 数据处理 ===//
    private void handleDataResponse(List<Article> newData) {
        isLoading.postValue(false);
        hasMore = !newData.isEmpty();

        List<Article> currentList = searchResultsLiveData.getValue() != null ?
                new ArrayList<>(searchResultsLiveData.getValue()) : new ArrayList<>();

        if (currentPage == 0) {
            currentList = newData; // 新搜索时完全替换
        } else {
            currentList.addAll(newData); // 加载更多时追加
        }

        searchResultsLiveData.postValue(currentList);
    }

    private void handleError(Throwable t) {
        isLoading.postValue(false);
        errorMsg.postValue(t.getMessage());
        if (currentPage > 0) currentPage--; // 回退页码
    }



    //=== 暴露的LiveData ===//
    public LiveData<List<Article>> getSearchResults() {
        return searchResultsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    public boolean hasMoreResults() {
        return hasMore;
    }
}