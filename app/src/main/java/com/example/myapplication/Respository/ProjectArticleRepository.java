package com.example.myapplication.Respository;

import android.util.Log;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.ProjectArticle;
import com.example.myapplication.model.ProjectArticleResponse;
import com.example.myapplication.model.ProjectCategory;
import com.example.myapplication.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ProjectArticleRepository.java
public class ProjectArticleRepository {
    private final ApiService apiService;

    public ProjectArticleRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    public void getProjectTabs(DataCallback<List<ProjectCategory>> callback) {
        apiService.getProjectTree().enqueue(new retrofit2.Callback<BaseResponse<List<ProjectCategory>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ProjectCategory>>> call,
                                   Response<BaseResponse<List<ProjectCategory>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //这步骤是获取对应的json转为实体吗
                    BaseResponse<List<ProjectCategory>> body = response.body();

                    if (body.getErrorCode() == 0) {
                        callback.onSuccess(body.getData());
                    } else {
                        callback.onFailure(body.getErrorMsg());
                    }
                } else {
                    callback.onFailure("Response not successful");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ProjectCategory>>> call, Throwable t) {
                Log.e("API_ERROR", "Network failure: " + t.getMessage(), t);
                callback.onFailure(t.getMessage());
            }
        });
    }

    //chapterName collect desc niceDate superChapterName envelopePic Title author
    public void getArticlesByTab(int tabId, int page, Repository.Callback<List<ProjectArticle>> callback) {
        Log.d("ProjectRepo", "getArticlesByTab - tabId: " + tabId + ", page: " + page);
        apiService.getProjectByChapter(page, tabId)
                .enqueue(new Callback<ProjectArticleResponse>() {
                    @Override
                    public void onResponse(Call<ProjectArticleResponse> call, Response<ProjectArticleResponse> response) {
                        Log.d("Project_API_DEBUG", "Request URL: " + call.request().url());

                        if (response.isSuccessful() && response.body() != null) {
                            // 1. 解析API响应
                            List<ProjectArticle> articleList = mapToProjectArticles(response.body());
                            Log.d("Project_Repository", "成功获取项目文章，数量: " + articleList.size());

                            // 2. 成功回调
                            callback.onSuccess(articleList);
                        } else {
                            // 3. 错误处理
                            Log.e("Project_Repository", "响应失败，状态码: " + response.code());
                            String errorMsg = "获取项目文章失败：" +
                                    (response.message() != null ? response.message() : "未知错误");
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ProjectArticleResponse> call, Throwable t) {
                        Log.e("Project_Repository", "网络请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }


    private List<ProjectArticle> mapToProjectArticles(ProjectArticleResponse response) {
        List<ProjectArticle> result = new ArrayList<>();

        // 1. 检查response是否为null
        if (response == null) {
            Log.e("ProjectRepo", "ProjectArticleResponse对象为null");
            return result;
        }

        // 2. 检查errorCode
        if (response.getErrorCode() != 0) {
            Log.e("ProjectRepo", "API返回错误码: " + response.getErrorCode() +
                    ", 错误信息: " + response.getErrorMsg());
            return result;
        }

        // 3. 检查data字段
        ProjectArticleResponse.Data apiData = response.getData();
        if (apiData == null) {
            Log.e("ProjectRepo", "响应中的data字段为null");
            return result;
        }

        // 4. 检查文章列表
        List<ProjectArticle> apiArticles = apiData.getDatas();
        if (apiArticles == null || apiArticles.isEmpty()) {
            Log.d("ProjectRepo", "文章列表为空");
            return result;
        }

        // 5. 遍历映射数据
        for (ProjectArticle apiArticle : apiArticles) {
            // 5.1 检查单个文章对象
            if (apiArticle == null) {
                Log.w("ProjectRepo", "单个文章对象为null，跳过处理");
                continue;
            }

            // 5.2 创建目标对象并映射字段
            ProjectArticle article = new ProjectArticle();

            // 必须字段（带空值处理）
            article.setTitle(apiArticle.getTitle() != null ? apiArticle.getTitle() : "无标题");
            String author = apiArticle.getAuthor();
            article.setAuthor(
                    (author != null && !author.trim().isEmpty())
                            ? author
                            : "佚名"
            );
            article.setChapterName(apiArticle.getChapterName() != null ? apiArticle.getChapterName() : "");
            article.setSuperChapterName(apiArticle.getSuperChapterName() != null ? apiArticle.getSuperChapterName() : "");
            article.setNiceDate(apiArticle.getNiceDate() != null ? apiArticle.getNiceDate() : "");
            article.setDesc(apiArticle.getDesc() != null ? apiArticle.getDesc() : "");
            article.setEnvelopePic(apiArticle.getEnvelopePic() != null ? apiArticle.getEnvelopePic() : "");
            article.setLink(apiArticle.getLink());
            // 布尔值/数值字段（直接映射）
            article.setCollect(apiArticle.isCollect());
            article.setId(apiArticle.getId());
            article.setPublishTime(apiArticle.getPublishTime());

            result.add(article);
        }

        return result;
    }

}