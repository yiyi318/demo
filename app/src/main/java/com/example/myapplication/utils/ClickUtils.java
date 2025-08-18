package com.yourpackage.util;

import android.view.View;

public class ClickUtils {
    private static final long DEFAULT_THROTTLE_TIME = 500; // 默认防抖间隔 500ms
    private static long lastClickTime = 0;

    /**
     * 设置防抖点击事件（全局防抖，所有按钮共用同一个时间戳）
     * @param view 目标控件
     * @param throttleTime 防抖时间（毫秒）
     * @param listener 点击回调
     */
    public static void setOnThrottleClickListener(View view, long throttleTime, View.OnClickListener listener) {
        view.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime >= throttleTime) {
                lastClickTime = currentTime;
                listener.onClick(v);
            }
        });
    }

    // 重载方法（使用默认防抖时间）
    public static void setOnThrottleClickListener(View view, View.OnClickListener listener) {
        setOnThrottleClickListener(view, DEFAULT_THROTTLE_TIME, listener);
    }
}