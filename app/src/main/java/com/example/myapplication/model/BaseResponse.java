package com.example.myapplication.model;

public class BaseResponse<T> {
    private int errorCode;  // 错误码（0表示成功）
    private String errorMsg; // 错误信息
    private T data;        // 实际数据

    // getters and setters
    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                ", data=" + data +
                '}';
    }

    public void setCode(int i) {
        errorCode=i;
    }

    public void setMessage(String message) {
        errorMsg=message;
    }
}
