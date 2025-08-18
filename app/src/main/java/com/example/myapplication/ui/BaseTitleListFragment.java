package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentBaseTitleListBinding;
import com.example.myapplication.ui.search.SearchActivity;
import com.example.myapplication.utils.drawer.NavigationDrawerFragment;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

/**
 * BaseTitleListFragment 是一个抽象 Fragment 类，用于构建具有标题列表、导航抽屉、搜索功能和 ViewPager2 的界面。
 * 子类需要实现 getTitles() 方法以提供标题列表。
 */
public abstract class BaseTitleListFragment extends Fragment {
    private static final String TAG = "BaseTitleListFragment";

    protected FragmentBaseTitleListBinding binding;
    private DrawerLayout.DrawerListener drawerListener;
    private NavigationDrawerFragment navDrawerFragment;


    /**
     * 创建 Fragment 的视图。
     *
     * @param inflater           用于加载布局的 LayoutInflater
     * @param container          父容器 ViewGroup
     * @param savedInstanceState 保存 Fragment 状态的 Bundle
     * @return Fragment 的根视图
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBaseTitleListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 在 Fragment 视图创建完成后进行初始化操作。
     *
     * @param view               Fragment 的根视图
     * @param savedInstanceState 保存 Fragment 状态的 Bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initNavigationDrawer();
        setupDrawerControls();
        setupSearchControls();
    }

    /**
     * 初始化并添加 NavigationDrawerFragment 到布局中。
     */
    private void initNavigationDrawer() {
        navDrawerFragment = new NavigationDrawerFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_1, navDrawerFragment)
                .commit();
    }

    /**
     * 设置搜索按钮的图标和点击事件，点击后跳转到 SearchActivity。
     */
    private void setupSearchControls() {
        binding.drawtoolbar.btnSave.setImageResource(R.drawable.ic_search);
        binding.drawtoolbar.btnSave.setOnClickListener(v -> goToSearchActivity());
    }

    /**
     * 跳转到 SearchActivity。
     */
    private void goToSearchActivity() {
        Intent intent = new Intent(getContext(), SearchActivity.class);
        startActivity(intent);
        Log.d(TAG, "跳转到 SearchActivity");
    }

    /**
     * 设置抽屉菜单按钮的图标和点击事件，并添加 DrawerListener 监听抽屉状态变化。
     */
    private void setupDrawerControls() {
        binding.drawtoolbar.qrCodeButton.setImageResource(R.drawable.ic_menu);
        binding.drawtoolbar.qrCodeButton.setOnClickListener(v -> {
            toggleDrawer();
        });

        drawerListener = new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                binding.mainContent.setClickable(false);//主要内容区域设置不可以点击
                navDrawerFragment.refreshData();//每次打开会刷新数据
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                binding.mainContent.setClickable(true);//主要内容区域设置可以点击
            }
        };

        binding.fragmentContainer.addDrawerListener(drawerListener);//添加监听
    }

    /**
     * 切换抽屉的打开/关闭状态。
     */
    private void toggleDrawer() {
        if (binding.fragmentContainer.isDrawerOpen(GravityCompat.START)) {
            binding.fragmentContainer.closeDrawer(GravityCompat.START);
        } else {
            binding.fragmentContainer.openDrawer(GravityCompat.START);
        }
    }

    /**
     * 获取标题列表，用于 TabLayout 和 ViewPager2 的页面标题。
     *
     * @return 标题字符串列表
     */
    public abstract List<String> getTitles();



    /**
     * 使用 Fragment 列表初始化 ViewPager2，并与 TabLayout 绑定。
     *
     * @param fragments Fragment 列表
     */
    protected void initViewPagerWithFragments(List<Fragment> fragments) {
        // 1. 获取标题列表
        List<String> titles = getTitles();
        if (titles == null || fragments.size() != titles.size()) {
            Log.e(TAG, "Fragment 列表与标题列表数量不一致");
            return;
        }

        // 2. 直接使用布局文件中预定义的 ViewPager2（无需动态创建）
        ViewPager2 viewPager = binding.viewPager; // 通过 ViewBinding 获取

        // 3. 设置 ViewPager2 适配器
        viewPager.setAdapter(new MyFragmentStateAdapter(this, fragments));

        // 4. 关联 TabLayout 和 ViewPager2
        new TabLayoutMediator(
                binding.tabLayout,
                viewPager,
                (tab, position) -> tab.setText(titles.get(position))
        ).attach();

    }

    /**
     * 在 Fragment 销毁视图时移除 DrawerListener 并清理资源。
     */
    @Override
    public void onDestroyView() {
        if (binding != null && drawerListener != null) {
            binding.fragmentContainer.removeDrawerListener(drawerListener);
        }
        super.onDestroyView();
        binding = null;
    }

    // 将 FragmentStateAdapter 提取为具名内部类
    private static class MyFragmentStateAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments;

        public MyFragmentStateAdapter(@NonNull Fragment fragment, List<Fragment> fragments) {
            super(fragment);
            this.fragments = fragments;
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }
    }
}
