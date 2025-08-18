package com.example.myapplication.viewmodel.system;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticleListViewModel extends ViewModel {
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> showFooter = new MutableLiveData<>(false);

    private final Repository repository;
    private int currentPage = 0;
    private int chapterId = -1;

    public ArticleListViewModel(Repository repository) {
        this.repository = repository;
    }

    public LiveData<List<Article>> getArticles() {
        return articles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    public LiveData<Boolean> getHasMore() {
        return hasMore;
    }

    public LiveData<Boolean> getShowFooter() {
        return showFooter;
    }

    public void setShowFooter(boolean show) {
        showFooter.postValue(show);
    }

    public void setChapterId(int newChapterId) {
        if (this.chapterId == newChapterId) return;
        this.chapterId = newChapterId;
        refreshArticles();
    }

    public void refreshArticles() {
        if (Boolean.TRUE.equals(isLoading.getValue())) return;
        currentPage = 0;
        hasMore.setValue(true);
        showFooter.setValue(false);
        loadArticles();
    }

    public void loadMoreArticles() {
        if (Boolean.TRUE.equals(isLoading.getValue())) return;
        if (!Boolean.TRUE.equals(hasMore.getValue())) {
            return;
        }
        currentPage++;
        loadArticles();
    }

    private void loadArticles() {
        if (chapterId == -1) {
            errorMsg.setValue("请先设置章节ID");
            return;
        }

        isLoading.setValue(true);

        repository.loadSystemArticles(currentPage, chapterId, new Repository.Callback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> result) {
                boolean noMore = result == null || result.isEmpty();
                hasMore.setValue(!noMore);
                isLoading.setValue(false);

                if (currentPage == 0) {
                    articles.setValue(result);
                } else {
                    List<Article> currentList = articles.getValue() != null
                            ? new ArrayList<>(articles.getValue())
                            : new ArrayList<>();
                    currentList.addAll(result);
                    articles.setValue(currentList);
                }

                showFooter.setValue(noMore);
            }

            @Override
            public void onFailure(Throwable t) {
                isLoading.setValue(false);
                if (currentPage > 0) currentPage--;
                errorMsg.setValue("加载失败：" + (t.getMessage() != null ? t.getMessage() : ""));
            }
        });
    }

}
