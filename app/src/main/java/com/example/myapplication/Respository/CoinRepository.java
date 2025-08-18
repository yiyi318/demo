package com.example.myapplication.Respository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.CoinArticle;
import com.example.myapplication.model.CoinArticleResponse;
import com.example.myapplication.model.CoinInfo;
import com.example.myapplication.model.RankResponse;
import com.example.myapplication.model.Resource;
import com.example.myapplication.network.ApiService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// CoinRepository.java
public class CoinRepository {
    private final ApiService apiService;

    public CoinRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // 获取排行榜
    public LiveData<Resource<List<CoinInfo>>> getCoinRankList(int page) {
        MutableLiveData<Resource<List<CoinInfo>>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));

        apiService.getCoinRankList(page).enqueue(new retrofit2.Callback<RankResponse>() {
            @Override
            public void onResponse(Call<RankResponse> call, Response<RankResponse> response) {
                Log.d("API_DEBUG", "Request URL: " + call.request().url());
                if (response.isSuccessful()) {
                    RankResponse body = response.body();
                    if (body != null && body.isSuccess()) {
                        result.postValue(Resource.success(body.getRankList()));
                    } else {
//                        result.postValue(Resource.error(
//                                body != null ? body.getErrorMsg() : "数据解析错误",
//                                null
//                        ));
                    }
                } else {
                    result.postValue(Resource.error("HTTP错误: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<RankResponse> call, Throwable t) {
                result.postValue(Resource.error("网络请求失败: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<List<CoinArticle>>> getCoinArticleList(int page) {
        MutableLiveData<Resource<List<CoinArticle>>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        Log.d("coin", "getCoinArticleList: page=" + page);

        // 接口返回类型改为 CoinArticleResponse
        apiService.getCoinList(page).enqueue(new retrofit2.Callback<CoinArticleResponse>() {
            @Override
            public void onResponse(Call<CoinArticleResponse> call, Response<CoinArticleResponse> response) {
                Log.d("API_DEBUG_coin", "Request URL: " + call.request().url());
                if (response.isSuccessful()) {
                    CoinArticleResponse body = response.body();
                    if (body != null) {
                        // 判断业务是否成功（errorCode == 0）
                        if (body.getErrorCode() == 0) {
                            // 从 data.datas 中获取积分记录列表
                            List<CoinArticle> coinArticles = body.getData() != null
                                    ? body.getData().getDatas()
                                    : Collections.emptyList();
                            result.postValue(Resource.success(coinArticles));
                        } else {
                            // 业务错误（如权限问题、参数错误等）
                            String errorMsg = body.getErrorMsg() != null && !body.getErrorMsg().isEmpty()
                                    ? body.getErrorMsg()
                                    : "业务请求失败";
                            Log.e("API_DEBUG_coin", "业务错误: " + errorMsg);
                            result.postValue(Resource.error(errorMsg, null));
                        }
                    } else {
                        // 响应体为空
                        Log.e("API_DEBUG_coin", "数据解析错误：响应体为空");
                        result.postValue(Resource.error("数据解析错误", null));
                    }
                } else {
                    // HTTP 错误（非 2xx 状态码）
                    try {
                        String errorBody = response.errorBody() != null
                                ? response.errorBody().string()
                                : "无详细错误信息";
                        String errorMsg = "HTTP错误: " + response.code() + "，详情：" + errorBody;
                        Log.e("API_DEBUG_coin", errorMsg);
                        result.postValue(Resource.error(errorMsg, null));
                    } catch (IOException e) {
                        Log.e("API_DEBUG_coin", "读取HTTP错误信息失败", e);
                        result.postValue(Resource.error("HTTP错误: " + response.code(), null));
                    }
                }
            }

            @Override
            public void onFailure(Call<CoinArticleResponse> call, Throwable t) {
                // 网络请求失败（如无网络、超时等）
                String errorMsg = "网络请求失败: " + t.getMessage();
                Log.e("API_DEBUG_coin", errorMsg + "，URL: " + call.request().url(), t);
                result.postValue(Resource.error(errorMsg, null));
            }
        });

        return result;
    }



    public LiveData<Resource<CoinInfo>> getMyCoins() {
        MutableLiveData<Resource<CoinInfo>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));

        apiService.getPersonalCoinInfo().enqueue(new Callback<BaseResponse<CoinInfo>>() {
            @Override
            public void onResponse(Call<BaseResponse<CoinInfo>> call, Response<BaseResponse<CoinInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CoinInfo coinInfo = response.body().getData();
//                    if (coinInfo != null) {
//                        int coinCount = coinInfo.getCoinCount();
                    result.postValue(Resource.success(coinInfo));
                } else {
                    result.postValue(Resource.error("获取积分失败", null));
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<CoinInfo>> call, Throwable t) {
                Log.d("API_DEBUG_coin", "网络请求失败" + call.request().url());
                result.postValue(Resource.error(t.getMessage(), null));
            }
        });

        return result;
    }
}