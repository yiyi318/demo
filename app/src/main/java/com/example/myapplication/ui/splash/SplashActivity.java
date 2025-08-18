package com.example.myapplication.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.scwang.smart.drawable.paint.BuildConfig;

/**
 * 启动页Activity类
 * <p>
 * 该类用于展示应用启动时的欢迎界面，包含一个Logo动画，
 * 动画播放完毕后自动跳转到主页面。
 * </p>
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int NO_ANIMATION_TRANSITION = 0;


    /**
     * Activity创建时的回调方法，用于初始化Splash页面
     * @param savedInstanceState 保存的实例状态，用于恢复Activity状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initViews();
    }

    /**
     * 设置Splash页面的视图组件，包括加载Logo动画并启动播放
     */
    private void initViews() {
        // 查找并验证Splash Logo ImageView
        ImageView splashLogo = findSplashLogo();
        if (splashLogo == null) {
            handleInitializationError("Splash logo ImageView not found");
            return;
        }

        // 加载并验证Logo动画
        Animation logoAnimation = loadLogoAnimation();
        if (logoAnimation == null) {
            handleInitializationError("Failed to load animation");
            return;
        }

        // 启动Logo动画播放
        startLogoAnimation(splashLogo, logoAnimation);
    }



    /**
     * 查找启动页Logo ImageView
     *
     * @return Logo ImageView实例，如果未找到返回null
     */
    private ImageView findSplashLogo() {
        try {
            return findViewById(R.id.iv_splash_logo);
        } catch (Exception e) {
            Log.e(TAG, "Error finding splash logo", e);
            return null;
        }
    }

    /**
     * 加载Logo动画资源
     *
     * @return Animation实例，如果加载失败返回null
     */
    private Animation loadLogoAnimation() {
        try {
            // 注意：应使用 R.anim 而非 R.animator
            return AnimationUtils.loadAnimation(this, R.animator.logo_animation);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load animation", e);
            return null;
        }
    }

    /**
     * 启动Logo动画并设置监听器
     *
     * @param splashLogo     Logo ImageView
     * @param logoAnimation  Logo动画
     */
    private void startLogoAnimation(ImageView splashLogo, Animation logoAnimation) {
        splashLogo.setVisibility(View.VISIBLE);
        splashLogo.startAnimation(logoAnimation);

        logoAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                logAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                logAnimationEnd(animation);
                navigateToMainActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 动画重复时不需要特殊处理
            }
        });
    }

    /**
     * 记录动画开始日志
     *
     * @param animation 动画实例
     */
    private void logAnimationStart(Animation animation) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Animation started with duration: " + animation.getDuration());
        }
    }

    /**
     * 记录动画结束日志
     *
     * @param animation 动画实例
     */
    private void logAnimationEnd(Animation animation) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Animation ended with duration: " + animation.getDuration());
        }
    }

    /**
     * 跳转到主页面
     */
    private void navigateToMainActivity() {
        if (isActivityValid()) {

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(NO_ANIMATION_TRANSITION, NO_ANIMATION_TRANSITION);
            //避免返回到启动页：用户在 MainActivity 按返回键时，不会重新回到 SplashActivity
            finish();
        }
    }

    /**
     * 检查当前Activity是否仍然有效
     *
     * @return true表示Activity有效，false表示无效
     */
    private boolean isActivityValid() {
        return !isFinishing() && !isDestroyed();
    }

    /**
     * 处理初始化错误
     *
     * @param errorMessage 错误信息
     */
    private void handleInitializationError(String errorMessage) {
        Log.e(TAG, errorMessage);
        // 可以添加用户提示，例如Toast或Snackbar
        navigateToMainActivity(); // 即使动画失败也尝试跳转到主页
    }
}
