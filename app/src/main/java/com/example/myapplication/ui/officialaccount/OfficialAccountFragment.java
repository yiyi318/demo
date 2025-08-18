package com.example.myapplication.ui.officialaccount;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.ui.BaseTitleListFragment;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.ProjectCategory;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.viewmodel.official.OfficialAccountViewModel;
import com.example.myapplication.viewmodel.official.OfficialSecondViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公众号页面 Fragment
 * 继承自 BaseTitleListFragment，用于展示公众号分类列表和对应的文章内容
 * 通过 TabLayout + ViewPager2 的方式展示不同公众号的文章列表
 */
public class OfficialAccountFragment extends BaseTitleListFragment {
    private OfficialAccountViewModel systemViewModel;
    private final Map<Integer, OfficialSecondViewModel> tabViewModels = new HashMap<>();

    /**
     * 创建 OfficialAccountFragment 实例
     *
     * @return 配置好的 OfficialAccountFragment 实例
     */
    public static OfficialAccountFragment newInstance() {
        return new OfficialAccountFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 设置工具栏
        setupToolbar();
        // 初始化ViewModel
        initViewModels();
        // 观察标签页数据变化
        observeTabData();
    }

    /**
     * 设置工具栏标题
     */
    private void setupToolbar() {
        if (binding != null && binding.drawtoolbar != null && binding.drawtoolbar.toolbarTitle != null) {
            binding.drawtoolbar.toolbarTitle.setText(R.string.official_account_title);
        }
    }

    /**
     * 初始化 ViewModels
     */
    private void initViewModels() {
        Repository repository = new Repository(RetrofitClient.getService());
        systemViewModel = new ViewModelProvider(this, new OfficialAccountViewModelFactory(repository))
                .get(OfficialAccountViewModel.class);
    }

    /**
     * 观察标签数据变化
     */
    private void observeTabData() {
        if (systemViewModel == null) return;

        systemViewModel.getTabs().observe(getViewLifecycleOwner(), this::updateTabFragments);
    }

    /**
     * 更新标签页 Fragment
     *
     * @param tabs 公众号分类列表
     */
    private void updateTabFragments(List<ProjectCategory> tabs) {
        if (tabs == null || tabs.isEmpty()) return;

        List<Fragment> fragments = createTabFragments(tabs);
        if (!fragments.isEmpty()) {
            initViewPagerWithFragments(fragments);
        }
    }

    /**
     * 创建标签页 Fragment 列表
     *
     * @param tabs 公众号分类列表
     * @return Fragment 列表
     */
    private List<Fragment> createTabFragments(List<ProjectCategory> tabs) {
        List<Fragment> fragments = new ArrayList<>();
        for (ProjectCategory tab : tabs) {
            try {
                fragments.add(OfficialArticleFragment.newInstance(tab.getId()));
            } catch (Exception e) {
                // 记录异常但不中断整个流程
                fragments.add(new Fragment()); // 添加空 Fragment 作为占位符
            }
        }
        return fragments;
    }

    @Override
    public List<String> getTitles() {
        if (systemViewModel != null && systemViewModel.getTabs().getValue() != null) {
            return extractTabTitles(systemViewModel.getTabs().getValue());
        }
        return Collections.emptyList();
    }

    /**
     * 提取标签标题列表
     *
     * @param tabs 公众号分类列表
     * @return 标题字符串列表
     */
    private List<String> extractTabTitles(List<ProjectCategory> tabs) {
        List<String> titles = new ArrayList<>();
        if (tabs != null) {
            for (ProjectCategory tab : tabs) {
                if (tab != null && tab.getName() != null) {
                    titles.add(tab.getName());
                }
            }
        }
        return titles;
    }

    /**
     * 获取或创建指定 Tab 的 ViewModel
     * 使用 computeIfAbsent 方法确保每个 Tab ID 只创建一个 ViewModel 实例
     *
     * @param tabId 公众号 Tab ID
     * @return 对应的 OfficialSecondViewModel 实例
     */
    private OfficialSecondViewModel getOrCreateViewModel(int tabId) {
        return tabViewModels.computeIfAbsent(tabId, id -> {
            Repository repository = new Repository(RetrofitClient.getService());
            OfficialSecondViewModel viewModel = new OfficialSecondViewModel(repository, id);
            viewModel.loadArticles();
            return viewModel;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupResources();
    }

    /**
     * 清理资源引用
     */
    private void cleanupResources() {
        tabViewModels.clear();
        systemViewModel = null;
    }

    /**
     * 公众号 ViewModel 工厂类，用于创建 OfficialAccountViewModel 实例
     */
    private static class OfficialAccountViewModelFactory implements ViewModelProvider.Factory {
        private final Repository repository;

        public OfficialAccountViewModelFactory(Repository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(OfficialAccountViewModel.class)) {
                return modelClass.cast(new OfficialAccountViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
