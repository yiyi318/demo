package com.example.myapplication.ui.project;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.ui.BaseTitleListFragment;
import com.example.myapplication.Respository.ProjectArticleRepository;
import com.example.myapplication.model.ProjectCategory;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.viewmodel.project.ProjectArticleViewModel;
import com.example.myapplication.viewmodel.project.ProjectSystemViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目页面 Fragment，用于展示项目分类和对应的项目文章列表。
 * 使用 ViewPager + Fragment 的方式展示不同分类的项目内容。
 */
public class ProjectFragment extends BaseTitleListFragment {
    private ProjectSystemViewModel systemViewModel;
    private final Map<Integer, ProjectArticleViewModel> tabViewModels = new HashMap<>();

    /**
     * 创建 ProjectFragment 实例
     *
     * @return 配置好的 ProjectFragment 实例
     */
    public static ProjectFragment newInstance() {
        return new ProjectFragment();
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
            binding.drawtoolbar.toolbarTitle.setText(R.string.project_title);
        }
    }

    /**
     * 初始化 ViewModels
     */
    private void initViewModels() {
        ProjectArticleRepository repository = new ProjectArticleRepository(RetrofitClient.getService());
        systemViewModel = new ViewModelProvider(this, new ProjectViewModelFactory(repository))
                .get(ProjectSystemViewModel.class);
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
     * @param tabs 项目分类列表
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
     * @param tabs 项目分类列表
     * @return Fragment 列表
     */
    private List<Fragment> createTabFragments(List<ProjectCategory> tabs) {
        List<Fragment> fragments = new ArrayList<>();
        for (ProjectCategory tab : tabs) {
            try {
                fragments.add(ProjectArticleTabFragment.newInstance(tab.getId()));
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
     * @param tabs 项目分类列表
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
     * 项目 ViewModel 工厂类，用于创建 ProjectSystemViewModel 实例
     */
    private static class ProjectViewModelFactory implements ViewModelProvider.Factory {
        private final ProjectArticleRepository repository;

        public ProjectViewModelFactory(ProjectArticleRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ProjectSystemViewModel.class)) {
                return modelClass.cast(new ProjectSystemViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
