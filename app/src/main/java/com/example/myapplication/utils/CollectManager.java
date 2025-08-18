package com.example.myapplication.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.ui.auth.AuthManager;

import retrofit2.Call;
import retrofit2.Response;

public class CollectManager {
    private static CollectManager instance;
    private final Repository repository;

    // 私有构造
    private CollectManager(Repository repository) {
        this.repository = repository;
    }

    // 初始化方法（在Application中调用）
    public static void init(Repository repo) {
        instance = new CollectManager(repo);
    }

    // 获取实例
    public static CollectManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("必须先调用CollectManager.init()初始化");
        }
        return instance;
    }

    /**
     * 修正后的收藏/取消收藏方法
     * @param articleId 文章ID
     * @param collect true=收藏, false=取消收藏
     * @param callback 结果回调
     */
    public void safeToggleCollect(Context context, int articleId, boolean collect, CollectCallback callback) {

        if (!AuthManager.isLoggedIn(context)) {
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show();
            return; // 直接返回，不执行后续操作
        }

        Call<BaseResponse> call = collect ?
                repository.collectArticle(articleId) :
                repository.uncollectArticle(articleId);

        call.enqueue(new retrofit2.Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(response.body().isSuccess());
                } else {
                    callback.onResult(false);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                callback.onResult(false);
            }
        });
    }



    public interface CollectCallback {
        void onResult(boolean success);
    }
}