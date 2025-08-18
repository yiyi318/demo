package com.example.myapplication.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentProfileBinding;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.utils.ProfileEventHandler;
import com.example.myapplication.utils.drawer.NavigationDrawerFragment;
import com.example.myapplication.viewmodel.SharedViewModel;

/**
 * 个人中心页面 Fragment，用于展示用户信息和各种功能入口。
 * 包括用户信息展示、侧边栏导航、积分系统等功能。
 */
public class ProfileFragment extends Fragment {
    private static final String USER_PREFS_NAME = "user_prefs";
    private static final String USERNAME_KEY = "username";
    private static final String RANK_KEY = "rank";
    private static final String LEVEL_KEY = "level";
    private static final String COIN_COUNT_KEY = "coin_count";
    private static final String DEFAULT_USERNAME = "已登录用户";
    private static final String DEFAULT_VALUE = "--";
    private static final String LOGIN_PROMPT = "点击头像登录";
    private FragmentProfileBinding binding;
    private ProfileEventHandler handler;
    private DrawerLayout.DrawerListener drawerListener;
    private NavigationDrawerFragment navDrawerFragment;
    private SharedViewModel sharedViewModel;

    /**
     * 创建 ProfileFragment 实例
     *
     * @return 配置好的 ProfileFragment 实例
     */
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initViewModel();
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
                // 初始化事件处理器
        initEventHandler();

        // 观察登录状态变化
        observeLoginState();

        // 初始化导航抽屉
        initNavigationDrawer();

        // 初始化视图组件
        initViews();

        // 设置点击事件监听器
        setupClickListeners();

    }

    /**
     * 初始化事件处理器
     */
    private void initEventHandler() {
        handler = new ProfileEventHandler(
                requireContext(),
                getParentFragmentManager(),
                this::updateUI
        );
    }

    /**
     * 观察登录状态变化
     */
    private void observeLoginState() {
        sharedViewModel.userLoggedIn.observe(getViewLifecycleOwner(), isLoggedIn -> updateUI());
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        setupToolbar();
        setupDrawerControls();
    }

    /**
     * 设置工具栏
     */
    private void setupToolbar() {
        if (binding == null) return;

        binding.profiletoolbar.toolbarTitle.setText(R.string.profile_center);
        binding.profiletoolbar.qrCodeButton.setImageResource(R.drawable.ic_menu);
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        setupAvatarClickListener();
        setupRankClickListener();
        setupMenuItemClickListeners();
        setupDrawerButtonClickListener();
    }

    /**
     * 设置头像点击监听器
     */
    private void setupAvatarClickListener() {
        if (binding == null) return;
        binding.content.ivAvatar.setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_AVATAR_CLICK);
            }
        });
    }

    /**
     * 设置积分排名点击监听器
     */
    private void setupRankClickListener() {
        if (binding == null) return;
        binding.content.ivRank.setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_RANK_CLICK);
            }
        });
    }

    /**
     * 设置菜单项点击监听器
     */
    private void setupMenuItemClickListeners() {
        if (binding == null) return;

        // 我的积分点击
        binding.content.itemPoints.getRoot().setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_POINTS_CLICK);
            }
        });

        // 我的收藏点击
        binding.content.itemFavorites.getRoot().setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_FAVORITES_CLICK);
            }
        });

        // 分享点击
        binding.content.itemShare.getRoot().setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_SHARE_CLICK);
            }
        });

        // 浏览历史点击
        binding.content.itemHistory.getRoot().setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_HISTORY_CLICK);
            }
        });

        // 设置点击
        binding.content.itemSettings.getRoot().setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_SETTINGS_CLICK);
            }
        });

        // 退出登录点击
        binding.content.itemBack.getRoot().setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_LOGOUT_CLICK);
            }
        });
    }

    /**
     * 设置抽屉按钮点击监听器
     */
    private void setupDrawerButtonClickListener() {
        if (binding == null) return;
        binding.profiletoolbar.qrCodeButton.setOnClickListener(v -> {
            toggleDrawer();
        });
    }

    /**
     * 刷新UI状态
     */
    public void updateUI() {
        // 防止binding为空时崩溃
        if (binding == null) {
            return;
        }
        Context context = requireContext();
        boolean isLoggedIn = AuthManager.isLoggedIn(context);

        // 控制退出按钮可见性
        updateLogoutButtonVisibility(isLoggedIn);

        if (isLoggedIn) {
            updateLoggedInUI(context);
        } else {
            updateLoggedOutUI();
        }
    }

    /**
     * 更新退出按钮可见性
     *
     * @param isVisible 是否可见
     */
    private void updateLogoutButtonVisibility(boolean isVisible) {
        if (binding == null) {
            return;
        }
        binding.content.itemBack.getRoot().setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 更新已登录状态下的UI
     *
     * @param context 上下文
     */
    private void updateLoggedInUI(Context context) {
        if (binding == null) return;
        SharedPreferences prefs = context.getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE);

        binding.content.tvUsername.setText(prefs.getString(USERNAME_KEY, DEFAULT_USERNAME));

        binding.content.ivAvatar.setImageResource(R.drawable.ic_profile_0);

        binding.content.rank.setText(prefs.getString(RANK_KEY, DEFAULT_VALUE));

        binding.content.level.setText(prefs.getString(LEVEL_KEY, DEFAULT_VALUE));

        binding.content.itemPoints.tvCoin.setText(prefs.getString(COIN_COUNT_KEY, DEFAULT_VALUE));
    }

    /**
     * 更新未登录状态下的UI
     */
    private void updateLoggedOutUI() {
        if (binding == null) return;

        binding.content.tvUsername.setText(LOGIN_PROMPT);

        binding.content.ivAvatar.setImageResource(R.drawable.ic_profile_1);

        binding.content.rank.setText(DEFAULT_VALUE);

        binding.content.level.setText(DEFAULT_VALUE);

        binding.content.itemPoints.tvCoin.setText(DEFAULT_VALUE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 确保每次进入页面时数据最新
        updateUI();
    }



    /**
     * 初始化导航抽屉
     */
    private void initNavigationDrawer() {
        try {
            navDrawerFragment = new NavigationDrawerFragment();
            if (isAdded()) {
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_1, navDrawerFragment)
                        .commitNow();
            }
        } catch (Exception e) {
            // 忽略异常，避免崩溃
        }
    }

    /**
     * 设置抽屉控件
     */
    private void setupDrawerControls() {
        setupDrawerButtonClickListener();
        initDrawerListener();
        addDrawerListener();
    }

    /**
     * 初始化抽屉监听器
     */
    private void initDrawerListener() {
        drawerListener = new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                performHapticFeedback();
                setMainContentClickable(false);
                refreshNavDrawerData();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                setMainContentClickable(true);
            }
        };
    }

    /**
     * 添加抽屉监听器
     */
    private void addDrawerListener() {
        if (binding == null || drawerListener == null) return;

        binding.drawerLayout.addDrawerListener(drawerListener);
    }

    /**
     * 切换抽屉状态
     */
    private void toggleDrawer() {
        if (binding == null) return;

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * 执行触觉反馈
     */
    private void performHapticFeedback() {
        if (binding == null) return;
        binding.profiletoolbar.qrCodeButton.performHapticFeedback(
                HapticFeedbackConstants.CONFIRM
        );
    }

    /**
     * 设置主内容是否可点击
     *
     * @param clickable 是否可点击
     */
    private void setMainContentClickable(boolean clickable) {
        if (binding == null) return;
        binding.mainContentProfile.setClickable(clickable);
    }

    /**
     * 刷新导航抽屉数据
     */
    private void refreshNavDrawerData() {
        if (navDrawerFragment != null) {
            navDrawerFragment.refreshData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理绑定
        binding = null;
        drawerListener = null;
        navDrawerFragment = null;
    }
}
