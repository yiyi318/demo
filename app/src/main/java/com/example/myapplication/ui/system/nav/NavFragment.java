package com.example.myapplication.ui.system.nav;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.adapter.NavCategoryAdapter;
import com.example.myapplication.adapter.WebsiteAdapter;
import com.example.myapplication.databinding.NavFragmentBinding;
import com.example.myapplication.model.WebsiteCategory;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.viewmodel.WebView.WebsiteViewModel;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

/**
 * 导航页面 Fragment，用于展示网站导航分类和对应的网站列表。
 * 左侧为分类列表，右侧为网站列表，采用 Flexbox 布局实现流式排列。
 */
public class NavFragment extends Fragment implements NavCategoryAdapter.OnCategoryClickListener {
    private NavFragmentBinding binding;
    private WebsiteViewModel viewModel;
    private NavCategoryAdapter categoryAdapter;
    private WebsiteAdapter websiteAdapter;

    /**
     * 创建 NavFragment 实例
     *
     * @return 配置好的 NavFragment 实例
     */
    public static NavFragment newInstance() {
        return new NavFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = NavFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /**
         * 设置分类RecyclerView的布局和基本配置
         */
        setupCategoryRecyclerView();

        /**
         * 设置网站RecyclerView的布局和基本配置
         */
        setupWebsiteRecyclerView();

        /**
         * 初始化并配置各个RecyclerView的适配器
         */
        setupAdapters();

        /**
         * 观察ViewModel中的数据变化，实现数据驱动的UI更新
         */
        observeViewModelData();

        /**
         * 从数据源加载初始数据
         */
        loadData();

    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        SystemRepository repository = new SystemRepository(RetrofitClient.getService());
        viewModel = new ViewModelProvider(this, new NavViewModelFactory(repository))
                .get(WebsiteViewModel.class);
    }

    /**
     * 设置分类 RecyclerView
     */
    private void setupCategoryRecyclerView() {
        binding.navCategories.setLayoutManager(createLinearLayoutManager());
    }

    /**
     * 设置网站 RecyclerView
     */
    private void setupWebsiteRecyclerView() {
        binding.navContents.setLayoutManager(createFlexboxLayoutManager());
        binding.navContents.addItemDecoration(createSpacingItemDecoration());
    }

    /**
     * 创建线性布局管理器
     *
     * @return LinearLayoutManager 实例
     */
    protected LinearLayoutManager createLinearLayoutManager() {
        return new LinearLayoutManager(requireContext());
    }

    /**
     * 创建 Flexbox 布局管理器
     *
     * @return FlexboxLayoutManager 实例
     */
    protected FlexboxLayoutManager createFlexboxLayoutManager() {
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(requireContext());
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_START);
        return flexboxLayoutManager;
    }

    /**
     * 创建间距装饰器
     *
     * @return RecyclerView.ItemDecoration 实例
     */
    protected RecyclerView.ItemDecoration createSpacingItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
                outRect.set(spacing, spacing, spacing, spacing);
            }
        };
    }

    /**
     * 初始化适配器
     */
    private void setupAdapters() {
        categoryAdapter = new NavCategoryAdapter(this);
        websiteAdapter = new WebsiteAdapter();
        binding.navCategories.setAdapter(categoryAdapter);
        binding.navContents.setAdapter(websiteAdapter);
    }

    /**
     * 观察 ViewModel 数据变化
     */
    private void observeViewModelData() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), this::updateCategoryList);
        viewModel.getCurrentWebsites().observe(getViewLifecycleOwner(), this::updateWebsiteList);
    }

    /**
     * 更新分类列表数据
     *
     * @param categories 分类列表数据
     */
    private void updateCategoryList(java.util.List<WebsiteCategory> categories) {
        categoryAdapter.submitList(categories);
        if (!categories.isEmpty()) {
            viewModel.selectCategory(categories.get(0).getCid());
        }
    }

    /**
     * 更新网站列表数据
     *
     * @param websites 网站列表数据
     */
    private void updateWebsiteList(java.util.List<com.example.myapplication.model.Website> websites) {
        websiteAdapter.setWebsites(websites);
    }

    /**
     * 加载初始数据
     */
    private void loadData() {
        viewModel.loadWebsiteCategories();
    }

    @Override
    public void onCategoryClick(WebsiteCategory category) {
        viewModel.selectCategory(category.getCid());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 导航 ViewModel 工厂类，用于创建 WebsiteViewModel 实例
     */
    private static class NavViewModelFactory implements ViewModelProvider.Factory {
        private final SystemRepository repository;

        public NavViewModelFactory(SystemRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(WebsiteViewModel.class)) {
                return modelClass.cast(new WebsiteViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
