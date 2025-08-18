package com.example.myapplication.viewmodel.Auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.AuthRepository;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.User;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthViewModel 类用于处理用户认证相关的业务逻辑，包括登录、注册和获取用户信息。
 * 它通过 AuthRepository 与数据源交互，并将结果暴露给 UI 层。
 */
public class AuthViewModel extends ViewModel {
    private final MutableLiveData<JsonObject> userInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<BaseResponse<User>> loginResult = new MutableLiveData<>();


    private final MutableLiveData<String> registerMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRegistering = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<Boolean> getIsRegistering() {
        return isRegistering;
    }

    /**
     * 获取登录结果的 LiveData
     *
     * @return 登录结果的 LiveData 对象
     */
    public LiveData<BaseResponse<User>> getLoginResult() {
        return loginResult;
    }

    /**
     * 获取注册消息的 LiveData
     *
     * @return 注册消息的 LiveData 对象
     */
    public LiveData<String> getRegisterMessage() {
        return registerMessage;
    }

    /**
     * 获取注册是否成功的 LiveData
     *
     * @return 注册成功状态的 LiveData 对象
     */
    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    /**
     * 执行用户注册操作
     *
     * @param username   用户名
     * @param password   密码
     * @param repassword 确认密码
     */
    public void register(String username, String password, String repassword) {
        isRegistering.setValue(true);
        repository.register(username, password, repassword, new Callback<>() {
            @Override
            public void onResponse(Call<BaseResponse<Object>> call, Response<BaseResponse<Object>> response) {
                isRegistering.setValue(false);
                handleRegisterResponse(response);
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> call, Throwable t) {
                isRegistering.setValue(false);
                handleRegisterFailure(t);
            }
        });
    }

    /**
     * 处理注册请求的成功响应
     *
     * @param response 注册请求的响应对象
     */
    private void handleRegisterResponse(Response<BaseResponse<Object>> response) {
        if (response.isSuccessful() && response.body() != null) {
            BaseResponse<Object> body = response.body();
            if (body.getErrorCode() == 0) {
                registerMessage.postValue("注册成功");
                registerSuccess.postValue(true);
            } else {
                registerMessage.postValue(body.getErrorMsg());
                registerSuccess.postValue(false);
            }
        } else {
            registerMessage.postValue("服务器返回错误");
            registerSuccess.postValue(false);
        }
    }

    /**
     * 处理注册请求的失败情况
     *
     * @param t 异常对象
     */
    private void handleRegisterFailure(Throwable t) {
        registerMessage.postValue("网络错误: " + t.getMessage());
        registerSuccess.postValue(false);
    }

    /**
     * 执行用户登录操作
     *
     * @param username 用户名
     * @param password 密码
     */


    public void login(String username, String password) {
        isLoading.setValue(true);
        repository.login(username, password).observeForever(result -> {
            loginResult.setValue(result);
            isLoading.setValue(false);
        });
    }

    /**
     * 获取当前用户的信息
     */
    public void fetchUserInfo() {
        repository.getUserInfo(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userInfoLiveData.postValue(response.body());
                } else {
                    errorLiveData.postValue("获取用户信息失败");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!call.isCanceled()) {
                    errorLiveData.postValue("网络错误: " + t.getMessage());
                }
            }
        });
    }

    /**
     * 获取用户信息的 LiveData
     *
     * @return 用户信息的 MutableLiveData 对象
     */
    public MutableLiveData<JsonObject> getUserInfoLiveData() {
        return userInfoLiveData;
    }

    /**
     * 获取错误信息的 LiveData
     *
     * @return 错误信息的 MutableLiveData 对象
     */
    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * 清除登录结果，避免观察到旧的登录数据
     */
    public void clearLoginResult() {
        loginResult.postValue(null);
    }




}
