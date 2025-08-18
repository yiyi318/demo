package com.example.myapplication.viewmodel.project;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.ProjectArticleRepository;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.ProjectArticle;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;

public class ProjectArticleViewModel extends ViewModel {
    // 数据存储与状态管理
    private final MutableLiveData<List<ProjectArticle>> articles = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final ProjectArticleRepository repository;


    // 分页参数
    private int tabId;
    private int currentPage = 0; // 当前页码（从0开始）

    // 将 hasMore 改为 MutableLiveData<Boolean>
    private final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(true);




    public ProjectArticleViewModel(ProjectArticleRepository repository, int tabId) {
        this.repository = repository;
        this.tabId = tabId;
    }

    // 对外暴露不可修改的 LiveData
    public LiveData<Boolean> getHasMore() {
        return hasMore;
    }

    // 对外暴露的LiveData（防止外部修改）
    public LiveData<List<ProjectArticle>> getArticles() {
        return articles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }


    /**
     * 刷新数据（重新加载第一页）
     */
    public void refreshArticles() {
        // 防止重复请求
        if (isLoading.getValue() == Boolean.TRUE) {
            Log.d("ProjectVM", "正在加载中，忽略刷新请求");
            return;
        }
        // 重置分页参数
        currentPage = 0;
        hasMore.setValue(true);

        // 加载数据
        loadArticles();
    }

    /**
     * 加载更多数据（加载下一页）
     */
    public void loadMoreArticles() {
        // 防止重复请求或无更多数据时请求
        if (isLoading.getValue() == Boolean.TRUE) {
            Log.d("ProjectVM", "正在加载中，忽略加载更多请求");
            return;
        }
        if (!hasMore.getValue()) {
            Log.d("ProjectVM", "没有更多数据，停止加载");
            return;
        }
        // 页码+1
        currentPage++;
        // 加载数据
        loadArticles();
    }

    /**
     * 实际加载数据的逻辑
     */
    public void loadArticles() {
        // 标记为加载中
        isLoading.setValue(true);
        // 调用仓库层获取数据
        repository.getArticlesByTab(tabId, currentPage, new Repository.Callback<List<ProjectArticle>>() {
            @Override
            public void onSuccess(List<ProjectArticle> result) {
                // 加载完成，更新状态
                isLoading.setValue(false);
                // 判断是否还有更多数据（根据返回结果是否为空）
                hasMore.setValue(!result.isEmpty());

                // 处理数据（刷新/加载更多）
                if (currentPage == 0) {
                    // 第一页数据直接覆盖
                    articles.setValue(result);
                    Log.d("ProjectVM", "刷新成功，数据量: " + result.size());
                } else {
                    // 分页数据追加到现有列表
                    List<ProjectArticle> currentList = articles.getValue() != null
                            ? new ArrayList<>(articles.getValue())
                            : new ArrayList<>();
                    currentList.addAll(result);
                    articles.setValue(currentList);
                    Log.d("ProjectVM", "加载更多成功，新增数据量: " + result.size() + "，总数据量: " + currentList.size());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // 加载失败，恢复状态
                isLoading.setValue(false);
                // 页码回退（避免下次请求跳过页码）
                if (currentPage > 0) {
                    currentPage--;
                }

                // 错误信息处理
                String errorMessage = "加载失败，请重试";
                if (t instanceof JsonParseException) {
                    errorMessage = "数据解析错误";
                } else if (t.getMessage() != null) {
                    errorMessage += "：" + t.getMessage();
                }
                errorMsg.setValue(errorMessage);
                Log.e("ProjectVM", "加载失败: " + errorMessage, t);
            }
        });
    }
}