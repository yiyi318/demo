package com.example.myapplication.viewmodel.system;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.model.QueryArticle;

import java.util.ArrayList;
import java.util.List;

public class QueryArticleViewModel extends ViewModel {
    private final SystemRepository repository;
    private final MutableLiveData<List<QueryArticle>> queryArticles = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private int currentPage = 0;
    private boolean hasMore = true;

    public QueryArticleViewModel(SystemRepository repository) {
        this.repository = repository;
    }

    public void refreshQueryArticles() {
        if (isLoading.getValue() == Boolean.TRUE) return;
        currentPage = 0;
        hasMore = true;
        loadQueryArticles();
    }

    public void loadMoreQueryArticles() {
        if (!hasMore || isLoading.getValue() == Boolean.TRUE) return;
        currentPage++;
        loadQueryArticles();
    }


    private void loadQueryArticles() {
        isLoading.setValue(true);
        repository.getQueryArticleListint(currentPage, new Repository.Callback<List<QueryArticle>>() {
            @Override
            public void onSuccess(List<QueryArticle> data) {
                isLoading.setValue(false);
                hasMore = !data.isEmpty();

                if (currentPage == 0) {
                    queryArticles.setValue(data);
                } else {
                    List<QueryArticle> current = queryArticles.getValue() != null ?
                            new ArrayList<>(queryArticles.getValue()) : new ArrayList<>();
                    current.addAll(data);
                    queryArticles.setValue(current);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                isLoading.setValue(false);
                errorMsg.setValue(t.getMessage());
                if (currentPage > 0) currentPage--;
            }
        });
    }

    public LiveData<List<QueryArticle>> getQueryArticles() {
        return queryArticles;
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
}