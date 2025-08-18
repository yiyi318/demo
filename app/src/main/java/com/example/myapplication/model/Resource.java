package com.example.myapplication.model;

// 文件路径：app/src/main/java/com/example/myapplication/model/Resource.java

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



/**
 * 统一封装网络请求状态（加载中/成功/失败）
 * @param <T> 数据类型
 */
public class Resource<T> {
    public enum Status { LOADING, SUCCESS, ERROR }

    public final Status status;
    public final T data;
    public final String message;

    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }
}
