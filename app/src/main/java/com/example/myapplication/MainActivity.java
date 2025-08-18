package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.officialaccount.OfficialAccountFragment;
import com.example.myapplication.ui.profile.ProfileFragment;
import com.example.myapplication.ui.profile.SettingFragment;
import com.example.myapplication.ui.project.ProjectFragment;
import com.example.myapplication.ui.system.SystemFragment;
import com.example.myapplication.utils.Broadcast.NetworkChangeReceiver;
import com.example.myapplication.utils.qrcode.QrCodeScanner;
import com.example.myapplication.utils.service.NetworkMonitorService;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity 是应用程序的主界面，负责管理底部导航栏、工具栏、Fragment切换、主题设置、扫码功能等核心功能。
 * 它实现了 QrCodeScanner.ScanResultCallback 接口以处理扫码结果。
 */
public class MainActivity extends AppCompatActivity implements QrCodeScanner.ScanResultCallback  {

    /**
     * Activity 生命周期内多次访问或操作的控件，最好声明为成员变量，然后在 onCreate 中通过 findViewById 初始化
     * 底部导航视图，用于在不同 Fragment 之间切换。
     */
    private BottomNavigationView navView;

    /**
     * 网络状态变化广播接收器。
     */
    private NetworkChangeReceiver receiver;

    /**
     * 双击退出的时间间隔（毫秒）。
     */
    private static final long EXIT_INTERVAL = 2000;

    /**
     * 上次按下返回键的时间戳。
     */
    private long mLastBackTime = 0;

    /**
     * 显示退出提示的 Toast。
     */
    private Toast mExitToast;

    /**
     * 主线程 Handler，用于处理 UI 相关任务。
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());


    /**
     * 共享 ViewModel，用于在不同组件之间共享数据。
     */
    private SharedViewModel sharedViewModel;

    /**
     * onCreate 方法是 Activity 生命周期的入口点，负责初始化主题、视图、服务、Fragment 等。
     *
     * @param savedInstanceState 保存的实例状态，用于恢复 Activity 状态。
     */
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 调用父类的onCreate方法，完成Activity的基本创建流程
        super.onCreate(savedInstanceState);
        // 设置当前Activity的布局文件
        setContentView(R.layout.activity_main);
        // 初始化ViewModel，用于数据管理和UI逻辑处理
        initViewModel();
        // 初始化广播接收器，用于接收和处理系统或其他应用发送的广播消息
        initReceiver();
        // 初始化视图组件，设置各种UI控件的属性和事件监听器
        initView();
        // 初始化网络请求客户端，配置Retrofit相关参数
        RetrofitClient.init(getApplicationContext());
        // 初始化homefragment
        initTabFragment();
        // 初始化Fragment，加载并显示各个功能模块的碎片界面
        initFragment();
        // 初始化后台服务，启动应用所需的常驻服务进程
        initService();

    }

    /**
     * 初始化 ViewModel 并观察底部导航栏可见性变化。
     */
    private void initViewModel() {
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.getShouldShowBottomTab().observe(this, this::setBottomTabVisibility);


    }


    /**
     * 启动网络监控服务。
     */
    private void initService() {
        startService(new Intent(this, NetworkMonitorService.class));
    }

    /**
     * 注册 Fragment 生命周期回调，用于调试 Fragment 显示情况。
     */
    private void initFragment() {
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        super.onFragmentResumed(fm, f);
                        Log.d("FragmentTrace", "显示 Fragment: " + f.getClass().getSimpleName());
                    }
                }, true);
    }

    /**
     初始化Homefragment
     */
    private void initTabFragment() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int tab = prefs.getInt("tab", -1); // Default to -1 if not found
        prefs.edit()
                .putInt("tab", -1)
                .apply();
        if (tab == -1 || tab == 0) {
            showFragment(new com.example.myapplication.HomeFragment());
        } else if(tab == 4) {
            showFragment(new SettingFragment());
        }
    }


    /**
     * 初始化视图组件，包括工具栏和底部导航栏。
     */
    private void initView() {
        initNavView();
    }

    /**
     * 初始化底部导航栏，设置点击事件和样式。
     */
    private void initNavView() {
        navView = findViewById(R.id.bottom_nav);
        navView.setItemIconTintList(null);
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;
            if (id == R.id.nav_home) {
                fragment = new com.example.myapplication.HomeFragment();
            } else if (id == R.id.nav_qa) {
                fragment = new ProjectFragment();
            } else if (id == R.id.nav_system) {
                fragment = new SystemFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (id == R.id.nav_officialaccount) {
                fragment = new OfficialAccountFragment();
            }
            if (fragment != null) {
                showFragment(fragment);
                return true;
            }
            return false;
        });
    }


    /**
     * 初始化网络状态变化广播接收器。
     */
    private void initReceiver() {
        receiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }



    /**
     * 处理返回键事件，支持双击退出应用和 Fragment 回退栈。
     */
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();//如果有fragment回退栈，则直接继承默认的onbackpressed
            return;
        }
        long currentTime = System.currentTimeMillis();//双击退出逻辑
        if (currentTime - mLastBackTime < EXIT_INTERVAL) {
            if (mExitToast != null) {
                mExitToast.cancel();
                mExitToast = null;
            }
            finish();
        } else {
            mLastBackTime = currentTime;
            showExitToast();
        }
    }



    /**
     * 显示退出提示 Toast，并在指定时间后自动消失。
     */
    private void showExitToast() {
        if (mExitToast != null) {
            mExitToast.cancel();
        }
        // 移除之前可能存在的延迟清理任务
        mHandler.removeCallbacksAndMessages(null);
        mExitToast = Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT);
        mExitToast.show();
//        两秒后关闭消息
        mHandler.postDelayed(() -> {
            if (mExitToast != null) {
                mExitToast.cancel();
                mExitToast = null;
            }
        }, EXIT_INTERVAL);
    }

//    /**
//     * 处理配置变化（如夜间模式切换），更新工具栏和状态栏颜色。
//     *
//     * @param newConfig 新的配置信息。
//     */
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        boolean isDarkMode = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
//        View root = findViewById(R.id.fragment_container); // 在 activity_main.xml 给根布局加 android:id="@+id/main_root"
//
//        if (isDarkMode) {
//            root.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
//        } else {
//            root.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
//        }
//
//        int bgColor = getThemeColor(androidx.transition.R.attr.colorPrimary);
//        updateStatusBarColor(bgColor, isDarkMode);
//        updateNavigationViewColor(bgColor);
//
//    }
//
//
//    /**
//     * 获取主题颜色。
//     *
//     * @param attr 主题属性 ID。
//     * @return 主题颜色值。
//     */
//    private int getThemeColor(int attr) {
//        TypedArray typedArray = obtainStyledAttributes(new int[]{attr});
//        int color = typedArray.getColor(0, Color.BLACK);
//        typedArray.recycle();
//        return color;
//    }
//
//    /**
//     * 更新底部导航栏背景颜色。
//     *
//     * @param bgColor 背景颜色。
//     */
//    private void updateNavigationViewColor(int bgColor) {
//        navView.setBackgroundTintList(ColorStateList.valueOf(bgColor));
//        navView.setItemRippleColor(null);
//        //异步刷新
//        navView.post(navView::requestLayout);
//    }
//
//    /**
//     * 更新状态栏颜色和图标颜色。
//     *
//     * @param statusBarColor 状态栏颜色。
//     * @param isDarkMode 是否为深色模式。
//     */
//    private void updateStatusBarColor(int statusBarColor, boolean isDarkMode) {
//        Window window = getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(statusBarColor);
//        View decorView = window.getDecorView();
//        int flags = decorView.getSystemUiVisibility();
//        if (isDarkMode) {
//            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//        } else {
//            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//        }
//        decorView.setSystemUiVisibility(flags);
//    }

    /**
     * 设置底部导航栏的可见性。
     *
     * @param visible 是否可见。
     */
    public void setBottomTabVisibility(boolean visible) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * @param fragment 要显示的 Fragment。
     */
    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }




    /**
     * 处理权限请求结果，如果扫码权限被授予则启动扫码功能，否则显示权限拒绝对话框。
     *
     * @param requestCode 请求码。
     * @param permissions 权限数组。
     * @param grantResults 权限授予结果数组。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == QrCodeScanner.REQUEST_PERMISSION) {
            boolean allGranted = true;
            //检查是否有不匹配 因为一个权限有几个功能需要开启
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                QrCodeScanner.showScanOptions();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    /**
     * 显示权限被拒绝对话框，提示用户前往设置开启权限。
     */
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("权限被拒绝")
                .setMessage("扫码功能需要相机和存储权限，请前往设置开启")
                .setPositiveButton("去设置", (dialog, which) -> openAppSettings())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 打开应用设置页面。
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    /**
     * 处理 Activity 返回结果，传递给扫码工具处理。
     *
     * @param requestCode 请求码。
     * @param resultCode 结果码。
     * @param data 返回数据。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        QrCodeScanner.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * 扫码成功回调，显示扫描结果。
     *
     * @param result 扫描结果字符串。
     */
    @Override
    public void onSuccess(String result) {
        runOnUiThread(() -> Toast.makeText(this, "扫描结果: " + result, Toast.LENGTH_SHORT).show());
    }

    /**
     * 扫码失败回调，显示错误信息。
     *
     * @param errorMessage 错误信息。
     */
    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show());
    }



    /**
     * Activity 销毁时调用，注销广播接收器、移除 Handler 消息并停止服务。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        stopService(new Intent(this, NetworkMonitorService.class));
    }

}

