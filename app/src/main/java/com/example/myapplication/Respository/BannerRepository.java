package com.example.myapplication.Repository;

import android.util.Log;

import com.example.myapplication.model.Banner;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BannerRepository {
    private final ApiService apiService;

    public BannerRepository() {
        // 从RetrofitClient获取API服务（建议后续改为依赖注入）
        this.apiService = RetrofitClient.getService();
    }

    public void getBanners(Callback<List<Banner>> callback) {
        apiService.getBanners()
                .enqueue(new retrofit2.Callback<BaseResponse<List<Banner>>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<List<Banner>>> call, Response<BaseResponse<List<Banner>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. 解析API响应中的Banner列表
                            List<Banner> bannerList = response.body().getData();
                            Log.d("BannerRepository", "成功获取Banner数据，数量: " + bannerList.size());
                            // 2. 成功回调
                            callback.onSuccess(bannerList);
                        } else {
                            // 3. 错误响应处理
                            Log.e("BannerRepository", "响应失败，状态码: " + (response.code() != 0 ? response.code() : "null"));
                            String errorMsg = "获取Banner失败：" + (response.message() != null ? response.message() : "未知错误");
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<List<Banner>>> call, Throwable t) {
                        // 4. 网络异常处理
                        Log.e("BannerRepository", "网络请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }



    // 复用原有的Callback接口（与ArticleRepository保持一致）
    public interface Callback<T> {
        void onSuccess(T data);
        void onFailure(Throwable t);
    }
}