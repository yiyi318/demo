package com.example.myapplication.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.R;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.ui.auth.LoginFragment;
import com.example.myapplication.ui.auth.RegisterFragment;
import com.example.myapplication.ui.profile.CoinFragment;
import com.example.myapplication.ui.profile.CoinRankFragment;
import com.example.myapplication.ui.profile.CollectFragment;
import com.example.myapplication.ui.profile.HistoryFragment;
import com.example.myapplication.ui.profile.SettingFragment;
import com.example.myapplication.ui.profile.ShareFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

public class ProfileEventHandler {
    // 事件类型常量
    public static final int EVENT_AVATAR_CLICK = 1;
    public static final int EVENT_RANK_CLICK = 2;
    public static final int EVENT_POINTS_CLICK = 3;
    public static final int EVENT_FAVORITES_CLICK = 4;
    public static final int EVENT_SHARE_CLICK = 5;
    public static final int EVENT_HISTORY_CLICK = 6;
    public static final int EVENT_SETTINGS_CLICK = 7;
    public static final int EVENT_LOGOUT_CLICK = 8;

    public interface OnUIUpdateListener {
        void onUpdateUI();
    }

    private final WeakReference<Context> contextRef;
    private final FragmentManager fragmentManager;
    private final OnUIUpdateListener updateListener;



    public ProfileEventHandler(Context context,
                               FragmentManager manager,
                               OnUIUpdateListener listener
) {
        this.contextRef = new WeakReference<>(context);
        this.fragmentManager = manager;
        this.updateListener = listener;
    }
    //处理对应的点击事件
    public void handleEvent(int eventType) {
        Context context = getValidContext();
        if (context == null) return;

        switch (eventType) {
            case EVENT_AVATAR_CLICK:
                handleAvatarClick(context);
                break;

            case EVENT_RANK_CLICK:
                replaceFragment(new CoinRankFragment());
                break;

            case EVENT_POINTS_CLICK:
                navigateWithAuthCheck(new CoinFragment());
                break;

            case EVENT_FAVORITES_CLICK:
                navigateWithAuthCheck(new CollectFragment());
                break;

            case EVENT_SHARE_CLICK:
                navigateWithAuthCheck(new ShareFragment());
                break;

            case EVENT_HISTORY_CLICK:
                replaceFragment(new HistoryFragment());
                break;

            case EVENT_SETTINGS_CLICK:
                replaceFragment(new SettingFragment());
                break;

            case EVENT_LOGOUT_CLICK:
                showLogoutDialog(context);
                break;
        }
    }



    private Context getValidContext() {
        Context context = contextRef.get();
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) return null;
        }
        return context;
    }
    //点击头像登录
    private void handleAvatarClick(Context context) {
        if (!AuthManager.isLoggedIn(context)) {
            showLoginChoiceDialog(context);
        }
    }

    //需要检查登录状态再展示页面
    private void navigateWithAuthCheck(Fragment targetFragment) {
        Context context = getValidContext();
        if (context == null) return;

        if (AuthManager.isLoggedIn(context)) {
            replaceFragment(targetFragment);
        } else {
            showLoginDialog(() -> {
                // 登录成功后再跳转
                replaceFragment(targetFragment);
                if (updateListener != null) updateListener.onUpdateUI();
            });
        }
    }

    private void showLoginChoiceDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("未登录")
                .setMessage("请选择登录或注册")
                .setPositiveButton("登录", (dialog, which) ->
                        showLoginDialog(() -> {

                        }))
                .setNegativeButton("注册", (dialog, which) ->
                        replaceFragment(new RegisterFragment()))
                .show();
    }

    //登录
    private void showLoginDialog(Runnable onSuccess) {
        Context context = getValidContext();
        if (context == null) return;

        LoginFragment fragment = new LoginFragment();
        fragment.setLoginSuccessListener(() -> {
            if (onSuccess != null) onSuccess.run(); // ✅ 调用外部传入的回调
            if (updateListener != null) updateListener.onUpdateUI();
        });

        replaceFragment(fragment);
        
    }
    //退出登录
    private void showLogoutDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("确认退出")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出", (dialog, which) -> {
                    AuthManager.logout(context);
                    Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show();
                    if (updateListener != null) updateListener.onUpdateUI();})
                .setNegativeButton("取消", null)
                .show();
    }



    // 替换fragment
    private void replaceFragment(Fragment fragment) {
        try {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        } catch (Exception e) {
            if (!fragmentManager.isDestroyed()) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commitAllowingStateLoss();
            }
        }
    }

}
