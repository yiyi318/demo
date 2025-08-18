package com.example.myapplication.Respository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 认证仓库类，负责处理用户认证相关的网络请求
 * 包括用户登录、注册和获取用户信息等功能
 */
public class AuthRepository {

    private final ApiService apiService;

    /**
     * 构造函数，初始化API服务实例
     */
    public AuthRepository() {
        this.apiService = RetrofitClient.getService();
    }

    /**
     * 用户登录功能
     * 通过用户名和密码进行登录验证，返回登录结果
     *
     * @param username 用户名
     * @param password 密码
     * @return LiveData<BaseResponse<User>> 登录结果的LiveData对象，包含用户信息或错误信息
     */
    public LiveData<BaseResponse<User>> login(String username, String password) {
        MutableLiveData<BaseResponse<User>> result = new MutableLiveData<>();

        // 发起登录请求
        apiService.login(username, password)
                .enqueue(new retrofit2.Callback<BaseResponse<User>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                        // 处理登录成功的响应
                        if (response.isSuccessful() && response.body() != null) {
                            result.postValue(response.body());
                        } else {
                            // 处理登录失败的情况
                            BaseResponse<User> fail = new BaseResponse<>();
                            fail.setCode(-1);
                            fail.setMessage("登录失败");
                            result.postValue(fail);
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
                        // 处理网络请求失败的情况
                        BaseResponse<User> fail = new BaseResponse<>();
                        fail.setCode(-1);
                        fail.setMessage("网络错误：" + t.getMessage());
                        result.postValue(fail);
                    }
                });

        return result;
    }


    /**
     * 用户注册功能
     * 通过用户名和密码进行用户注册
     *
     * @param username   用户名
     * @param password   密码
     * @param repassword 确认密码
     * @param callback   注册结果回调接口
     */
    public void register(String username, String password, String repassword,
                         Callback<BaseResponse<Object>> callback) {
        apiService.register(username, password, repassword).enqueue(callback);
    }

    /**
     * 获取用户信息功能
     * 获取当前登录用户的基本信息
     *
     * @param callback 用户信息回调接口
     */
    public void getUserInfo(Callback<JsonObject> callback) {
        apiService.getUserInfo().enqueue(callback);
    }


}

