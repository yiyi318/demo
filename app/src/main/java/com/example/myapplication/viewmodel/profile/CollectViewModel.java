package com.example.myapplication.viewmodel.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.Article;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectViewModel extends ViewModel {
    private static final String TAG = "CollectViewModel";
    private final ApiService apiService = RetrofitClient.getService();
    private Repository repository=new Repository(apiService);

    // 数据相关LiveData
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>();
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();

    // 加载状态相关
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);

    // 分页控制
    private int currentPage = 0;
    private boolean isAllLoaded = false;

    public LiveData<List<Article>> getArticles() { return articles; }
    public LiveData<String> getErrorMsg() { return errorMsg; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsRefreshing() { return isRefreshing; }

    /**
     * 初始化加载或刷新数据
     */
    public void refreshData() {
        if (Boolean.TRUE.equals(isRefreshing.getValue())) return;
        currentPage = 0;
        isAllLoaded = false;
        isRefreshing.setValue(true);
        errorMsg.setValue(null);
        loadCollectArticles(currentPage);
    }

    /**
     * 加载更多数据
     */
    public void loadMoreData() {
        isLoading.setValue(true);
        if (isAllLoaded){
            Log.d(TAG, "loadMoreData: 没有数据了");
            isLoading.setValue(false);
            errorMsg.setValue("没有更多内容了");
            return;

        }
        Log.d(TAG, "loadMoreData: 到这里了");
        loadCollectArticles(currentPage + 1);
    }


    private void loadCollectArticles(int page) {
        // 先判断是否已经加载完
        if (isAllLoaded && page != 0) return;

        repository.loadCollectArticle(page,new Repository.Callback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> newArticles) {
                if (page == 0) {
                    isRefreshing.setValue(false);
                }
                if (newArticles.isEmpty()) {
                    if (page == 0) {
                        // 第一页空数据，清空列表并显示无内容提示
                        articles.setValue(Collections.emptyList());
                        errorMsg.setValue("暂无收藏内容");
                    } else {
                        // 其他页无数据，标记加载完毕
                        isAllLoaded = true;
                        errorMsg.setValue("没有更多内容了");
                    }

                } else {
                    // 正常数据，更新列表，重置标志
                    updateArticles(page, newArticles);
                    isAllLoaded = false;
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Throwable t) {
                if (page == 0) {
                    isRefreshing.setValue(false);
                } else {
                    isLoading.setValue(false);
                }
                // 处理错误
                errorMsg.setValue("加载失败，请重试");
            }
        });
    }


    private void updateArticles(int page, List<Article> newArticles) {
        for (Article article : newArticles) {
            article.setCollect(true); // 强制设置为已收藏状态
        }

        if (page == 0) {
            articles.setValue(newArticles);
        } else {
            List<Article> current = articles.getValue();
            if (current != null) {
                current.addAll(newArticles);
                articles.setValue(current);
            }
        }
        currentPage = page;
    }

    public void updateArticlesList(List<Article> newList) {
        articles.setValue(newList);
    }

    public void uncollectArticle(int articleId, int originId, int position) {
        repository.uncollectArticle_2(articleId, originId, new Repository.Callback<>() {
            @Override
            public void onSuccess(BaseResponse data) {
                removeArticle(position);
            }

            @Override
            public void onFailure(Throwable t) {
                errorMsg.setValue("操作失败: " + t);
            }
        });
    }

    private void removeArticle(int position) {
        List<Article> currentList = articles.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            List<Article> newList = new ArrayList<>(currentList);
            newList.remove(position);
            updateArticlesList(newList);
        }
    }
}