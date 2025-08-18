package com.example.myapplication.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.example.myapplication.utils.DateDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 常量配置
    private static final String BASE_URL = "https://www.wanandroid.com/";
    private static final long CONNECT_TIMEOUT = 20; // 秒
    private static final long READ_TIMEOUT = 20;    // 秒
    private static final String COOKIE_PREFS_NAME = "OkHttpCookies";

    // 单例实例
    private static Retrofit retrofit;
    private static CustomCookieStore cookieStore;
    private static Context appContext;

    /**
     * 初始化方法（必须在Application中调用）
     */
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    /**
     * 获取Retrofit实例
     */
    public static synchronized Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient client = createOkHttpClient().build();
            logClientConfig(client);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    /**
     * 获取API服务实例
     */
    public static ApiService getService() {
        return getRetrofit().create(ApiService.class);
    }

    /**
     * 创建OkHttpClient构建器
     */
    private static OkHttpClient.Builder createOkHttpClient() {
        checkInitialization();

        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .cookieJar(new CustomCookieStore(appContext));
    }

    /**
     * 清除所有会话数据
     */
    public static synchronized void clearSession() {
        // 清除Retrofit实例
        retrofit = null;

        // 清除Cookie数据
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(COOKIE_PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            if (cookieStore != null) {
                cookieStore.clear();
            }
        }
    }

    /**
     * 检查是否已初始化
     */
    private static void checkInitialization() {
        if (appContext == null) {
            throw new IllegalStateException("必须先调用RetrofitClient.init(context)进行初始化");
        }
    }

    /**
     * 打印客户端配置日志
     */
    private static void logClientConfig(OkHttpClient client) {
        Log.d("RetrofitClient", "连接超时: " + client.connectTimeoutMillis() + "ms");
        Log.d("RetrofitClient", "读取超时: " + client.readTimeoutMillis() + "ms");
    }

    /**
     * 自定义Cookie存储实现
     */
    private static class CustomCookieStore implements CookieJar {
        private final Map<String, List<Cookie>> cookieStore = new HashMap<>();
        private final SharedPreferences prefs;

        CustomCookieStore(Context context) {
            prefs = context.getSharedPreferences(COOKIE_PREFS_NAME, Context.MODE_PRIVATE);
            loadPersistedCookies();
        }

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
            persistCookies(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<>();
        }

        void clear() {
            cookieStore.clear();
        }

        private void persistCookies(String host, List<Cookie> cookies) {
            SharedPreferences.Editor editor = prefs.edit();
            StringBuilder sb = new StringBuilder();

            for (Cookie cookie : cookies) {
                sb.append(serializeCookie(cookie)).append(";");
            }

            if (sb.length() > 0) {
                editor.putString(host, sb.toString());
            } else {
                editor.remove(host);
            }
            editor.apply();
        }

        private void loadPersistedCookies() {
            for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
                String encoded = (String) entry.getValue();
                if (!TextUtils.isEmpty(encoded)) {
                    List<Cookie> cookies = deserializeCookies(encoded);
                    if (!cookies.isEmpty()) {
                        cookieStore.put(entry.getKey(), cookies);
                    }
                }
            }
        }

        private String serializeCookie(Cookie cookie) {
            return cookie.name() + "=" + cookie.value() +
                    "|domain=" + cookie.domain() +
                    "|path=" + cookie.path() +
                    "|expires=" + cookie.expiresAt() +
                    "|secure=" + cookie.secure() +
                    "|httpOnly=" + cookie.httpOnly();
        }

        private List<Cookie> deserializeCookies(String encoded) {
            List<Cookie> cookies = new ArrayList<>();
            String[] cookieStrings = encoded.split(";");

            for (String cookieStr : cookieStrings) {
                if (!TextUtils.isEmpty(cookieStr)) {
                    Cookie cookie = deserializeCookie(cookieStr);
                    if (cookie != null) {
                        cookies.add(cookie);
                    }
                }
            }
            return cookies;
        }

        private Cookie deserializeCookie(String encoded) {
            try {
                String[] parts = encoded.split("\\|");
                String[] nameValue = parts[0].split("=");
                if (nameValue.length != 2) return null;

                Cookie.Builder builder = new Cookie.Builder()
                        .name(nameValue[0])
                        .value(nameValue[1]);

                for (int i = 1; i < parts.length; i++) {
                    String[] pair = parts[i].split("=");
                    if (pair.length != 2) continue;

                    switch (pair[0]) {
                        case "domain": builder.domain(pair[1]); break;
                        case "path": builder.path(pair[1]); break;
                        case "expires": builder.expiresAt(Long.parseLong(pair[1])); break;
                        case "secure": if (Boolean.parseBoolean(pair[1])) builder.secure(); break;
                        case "httpOnly": if (Boolean.parseBoolean(pair[1])) builder.httpOnly(); break;
                    }
                }

                return builder.build();
            } catch (Exception e) {
                Log.w("CookieDeserialize", "Failed to decode cookie: " + encoded, e);
                return null;
            }
        }
    }
}