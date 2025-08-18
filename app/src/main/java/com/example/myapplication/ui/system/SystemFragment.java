package com.example.myapplication.ui.system;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.ui.BaseTitleListFragment;
import com.example.myapplication.ui.system.nav.NavFragment;
import com.example.myapplication.ui.system.query.QueryArticleFragment;
import com.example.myapplication.ui.system.square.SquareArticleFragment;
import com.example.myapplication.ui.system.system.SystemCategoryFragment;
import com.example.myapplication.ui.system.tutorial.TutorialFragment;
import com.example.myapplication.viewmodel.SharedViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SystemFragment 是一个用于展示系统相关功能模块的 Fragment。
 * 它继承自 BaseTitleListFragment，通过 ViewPager 和 TabLayout 展示多个子功能页面，
 * 包括广场、每日一问、体系、导航和教程等模块。
 */
public class SystemFragment extends BaseTitleListFragment {
    // 定义Tab类型常量
    private SharedViewModel sharedViewModel;

    /**
     * 当 Fragment 的视图创建完成后调用。
     * 此方法会调用父类的 onViewCreated 方法，并初始化 Tab 和 Fragment 的配置。
     *
     * @param view               当前 Fragment 的根视图
     * @param savedInstanceState 用于恢复状态的 Bundle 对象，可能为 null
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        setupTabsAndFragments();
        initView();
    }

    private void initViewModel() {
        if (getActivity() != null) {
            sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            sharedViewModel.setShouldShowBottomTab(true);
        }
    }

    private void initView() {
        if (binding != null) {
            binding.drawtoolbar.toolbarTitle.setText(R.string.system_title);
        }
    }


    /**
     * 初始化并设置各个 Tab 对应的 Fragment 列表。
     * 将 Fragment 添加到列表中，并调用 initViewPagerWithFragments 方法进行 ViewPager 初始化。
     */
    private void setupTabsAndFragments() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(createFragment(SquareArticleFragment.class));       // 广场
        fragments.add(createFragment(QueryArticleFragment.class));        // 每日一问
        fragments.add(createFragment(SystemCategoryFragment.class));      // 体系
        fragments.add(createFragment(NavFragment.class));                 // 导航
        fragments.add(createFragment(TutorialFragment.class));            // 教程

        try {
            initViewPagerWithFragments(fragments);
        } catch (Exception e) {
            // 日志记录或错误提示
            e.printStackTrace();
        }
    }

    /**
     * 使用反射创建 Fragment 实例，便于后续扩展和参数传递
     *
     * @param clazz Fragment 类型
     * @return 新建的 Fragment 实例
     */
    private Fragment createFragment(Class<? extends Fragment> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("无法实例化 Fragment: " + clazz.getSimpleName(), e);
        } catch (java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取 Tab 标题列表。
     *
     * @return 包含所有 Tab 标题的字符串列表
     */
    @Override
    public List<String> getTitles() {
        return Arrays.asList("广场", "每日一问", "体系", "导航", "教程");
    }
}
