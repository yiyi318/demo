package com.example.myapplication.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myapplication.network.RetrofitClient;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// AuthManager.java
/**
 * 用户认证管理工具类
 * 负责用户登录状态管理、用户信息存储和登出操作
 * 使用 SharedPreferences 存储用户信息，RetrofitClient 管理网络会话
 */
public class AuthManager {
    // SharedPreferences 文件名
    private static final String PREFS_NAME = "user_prefs";

    // SharedPreferences 键名定义
    private static final String KEY_USERNAME = "username";
    private static final String KEY_COOKIE = "cookie";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_RANK = "rank";
    private static final String KEY_COIN_COUNT = "coin_count";
    private static final String KEY_NICKNAME = "nickname";

    /**
     * 保存用户登录信息
     *
     * @param context  上下文对象
     * @param username 用户名
     * @param cookie   认证Cookie
     */
    public static void saveLoginInfo(Context context, String username, String cookie) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_COOKIE, cookie)
                .apply();
    }

    /**
     * 保存用户详细信息
     * 包括等级、排名、积分和昵称等信息
     *
     * @param context       上下文对象
     * @param userInfoJson  包含用户信息的JSON对象
     */
    public static void saveUserInfo(Context context, JsonObject userInfoJson) {
        JsonObject coinInfo = userInfoJson.getAsJsonObject("coinInfo");
        JsonObject userInfo = userInfoJson.getAsJsonObject("userInfo");

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putString(KEY_LEVEL, String.valueOf(coinInfo.get("level").getAsInt()))
                .putString(KEY_RANK, coinInfo.get("rank").getAsString())
                .putString(KEY_COIN_COUNT, String.valueOf(coinInfo.get("coinCount").getAsInt()))
                .putString(KEY_NICKNAME, userInfo.get("nickname").getAsString())
                .apply();
    }

    /**
     * 检查用户是否已登录
     * 通过检查是否存在Cookie来判断登录状态
     *
     * @param context 上下文对象
     * @return 已登录返回true，否则返回false
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COOKIE, null) != null;
    }


    /**
     * 用户登出操作
     * 清除本地用户数据和网络会话，并调用服务端注销接口
     *
     * @param context 上下文对象
     */
    public static void logout(Context context) {
        // 1. 清除本地用户数据
        SharedPreferences userPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        // 2. 清除网络会话
        RetrofitClient.clearSession();

        // 3. 调用服务端注销API
        RetrofitClient.getService().logout().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("Auth", "服务端注销成功");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Auth", "服务端注销失败", t);
            }
        });
    }
}
