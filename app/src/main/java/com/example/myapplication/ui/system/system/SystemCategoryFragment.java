package com.example.myapplication.ui.system.system;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.adapter.SystemCategoryAdapter;
import com.example.myapplication.databinding.FragmentSystemCategoryBinding;
import com.example.myapplication.model.Chapter;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.viewmodel.system.SystemViewModel;

import java.util.List;

/**
 * 系统分类页面 Fragment，用于展示体系结构的一级分类列表。
 * 用户点击分类后跳转到二级页面查看该分类下的子分类和文章。
 */
public class SystemCategoryFragment extends Fragment {
    private FragmentSystemCategoryBinding binding;
    private SystemViewModel systemVM;
    private SystemCategoryAdapter adapter;


    /**
     * 创建 SystemCategoryFragment 实例
     *
     * @return 配置好的 SystemCategoryFragment 实例
     */
    public static SystemCategoryFragment newInstance() {
        return new SystemCategoryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化 ViewModel
        initViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用 ViewBinding 加载布局
        binding = FragmentSystemCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 设置 RecyclerView 及其适配器
        setupRecyclerView();
        // 观察 ViewModel 中的数据变化
        observeViewModelData();
        // 加载初始数据
        loadInitialData();
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        SystemRepository repository = new SystemRepository(RetrofitClient.getService());
        systemVM = new ViewModelProvider(this, new SystemViewModelFactory(repository))
                .get(SystemViewModel.class);
    }

    /**
     * 设置 RecyclerView 及其适配器
     */
    private void setupRecyclerView() {
        adapter = new SystemCategoryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        // 设置分类点击监听器
        adapter.setOnCategoryClickListener(this::handleCategoryClick);
    }


    /**
     * 观察 ViewModel 数据变化
     */
    private void observeViewModelData() {
        systemVM.getPrimaryCategories().observe(getViewLifecycleOwner(), this::updateCategoryData);
    }

    /**
     * 更新分类数据
     *
     * @param chapters 分类数据列表
     */
    private void updateCategoryData(List<Chapter> chapters) {
        if (chapters != null) {
            adapter.setData(chapters);
        }
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        systemVM.loadPrimaryCategories();
    }

    /**
     * 处理分类点击事件，跳转到二级页面
     *
     * @param children 子分类列表
     * @param name     分类名称
     */
    private void handleCategoryClick(List<Chapter> children, String name) {
        navigateToSecondaryFragment(children, name);
    }

    /**
     * 跳转到二级页面 Fragment
     *
     * @param children 子分类列表
     * @param name     分类名称
     */
    protected void navigateToSecondaryFragment(List<Chapter> children, String name) {
        SecondaryFragment fragment = SecondaryFragment.newInstance(children, name);

        try {
            getParentFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.main_content, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e("SystemCategory", "Fragment 跳转失败", e);
        }
    }
    // 在 SystemCategoryFragment 中添加
    @Override
    public void onResume() {
        super.onResume();
        Log.d("FragmentLife", "onResume: SystemCategoryFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("FragmentLife", "onPause: SystemCategoryFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("FragmentLife", "onStop: SystemCategoryFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理 ViewBinding 引用
        Log.d("FragmentLife", "SystemCategoryFragment 视图销毁");
        binding = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("FragmentLife", "SystemCategoryFragment 实例销毁");
    }

    /**
     * 系统 ViewModel 工厂类，用于创建 SystemViewModel 实例
     */
    private static class SystemViewModelFactory implements ViewModelProvider.Factory {
        private final SystemRepository repository;

        public SystemViewModelFactory(SystemRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SystemViewModel.class)) {
                return modelClass.cast(new SystemViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
