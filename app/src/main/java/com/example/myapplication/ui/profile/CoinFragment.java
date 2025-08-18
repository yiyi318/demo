package com.example.myapplication.ui.profile;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Respository.CoinRepository;
import com.example.myapplication.adapter.CoinArticleAdapter;
import com.example.myapplication.databinding.FragmentCoinBinding;
import com.example.myapplication.databinding.ToolbarBinding;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.viewmodel.profile.CoinViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.example.myapplication.R;

/**
 * 积分页面 Fragment，用于展示用户的积分信息和积分排行榜。
 * 提供积分列表的分页加载、下拉刷新功能，并通过 ViewModel 管理数据。
 */
public class CoinFragment extends Fragment {
    private CoinViewModel viewModel;
    private FragmentCoinBinding binding;
    private CoinArticleAdapter adapter;

    /**
     * 创建一个新的 CoinFragment 实例
     *
     * @return CoinFragment 新实例
     */
    public static CoinFragment newInstance() {
        return new CoinFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCoinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化ViewModel，设置数据绑定和业务逻辑处理
        initViewModel();

        // 设置工具栏，配置标题和导航功能
        setupToolbar();

        // 配置RecyclerView，设置布局管理器和适配器
        setupRecyclerView();

        // 设置下拉刷新布局，配置刷新监听器和刷新状态
        setupRefreshLayout();

        // 配置滚动监听器，处理列表滚动事件和分页加载
        setupScrollListener();

        // 设置数据观察者，监听LiveData数据变化并更新UI
        setupObservers();

        // 初始化数据加载，执行首次数据获取操作
        initLoadDate();
    }

    /**
     * 初始化数据加载，执行首次数据获取操作
     * 包括加载用户积分和刷新积分排行榜
     */
    private void initLoadDate() {
        // 加载我的积分数据
        viewModel.loadMyCoins();
        // 刷新积分排行榜数据
        viewModel.refreshRankList();
    }

    /**
     * 初始化ViewModel，创建CoinViewModel实例
     * 使用CoinRepository和Retrofit服务进行数据操作
     */
    private void initViewModel() {
        CoinRepository repository = new CoinRepository(RetrofitClient.getService());
        viewModel = new ViewModelProvider(this,
                new CoinViewModelFactory(repository)
        ).get(CoinViewModel.class);
    }

    /**
     * 设置工具栏，配置标题和返回按钮
     * 设置标题为"我的积分"，返回按钮点击事件为返回上一页
     */
    private void setupToolbar() {
        ToolbarBinding toolbarBinding = binding.cointoolbar;
        toolbarBinding.toolbarTitle.setText(R.string.my_coins);
        toolbarBinding.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        toolbarBinding.qrCodeButton.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * 配置RecyclerView，设置布局管理器、适配器和分割线
     * 使用LinearLayoutManager垂直排列，添加DividerItemDecoration分割线
     */
    private void setupRecyclerView() {
        adapter = new CoinArticleAdapter();
        binding.rvCoin.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCoin.setAdapter(adapter);

        // 添加Item间距装饰器
        binding.rvCoin.addItemDecoration(new DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
        ));
    }

    /**
     * 设置下拉刷新布局，配置刷新监听器和刷新头部样式
     * 下拉刷新时重新加载积分排行榜数据
     */
    private void setupRefreshLayout() {
        binding.coinRefreshLayout.setOnRefreshListener(refreshLayout ->
            viewModel.refreshRankList());
        // 设置经典风格的刷新头部
        binding.coinRefreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
    }

    /**
     * 配置滚动监听器，实现列表滚动到底部时自动加载更多数据
     * 使用RecyclerView的OnScrollListener监听滚动事件
     */
    private void setupScrollListener() {
        binding.rvCoin.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // 滑动到底部时加载更多
                if (!binding.coinRefreshLayout.isRefreshing() &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0) {
                    viewModel.getRankData();
                }
            }
        });
    }

    /**
     * 设置数据观察者，监听LiveData数据变化并更新UI
     * 分别观察积分数据和排行榜数据的变化
     */
    private void setupObservers() {
        // 观察用户积分数据变化
        viewModel.getCoinLiveData().observe(getViewLifecycleOwner(), coinCount -> {
            if (coinCount != null) {
                binding.tvMycoin.setText(String.valueOf(coinCount));
            }
        });
        // 观察积分排行榜数据变化
        viewModel.getRankData().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    // 如果是首次加载或刷新，显示加载状态
                    if (resource.data == null || resource.data.isEmpty()) {
                        showLoading();
                    }
                    break;
                case SUCCESS:
                    hideLoading();
                    binding.coinRefreshLayout.finishRefresh();
                    // 更新数据到Adapter
                    if (resource.data != null) {
                        adapter.setCoinArticles(resource.data);
                    }
                    break;
                case ERROR:
                    hideLoading();
                    binding.coinRefreshLayout.finishRefresh(false);
                    // 显示错误信息（仅在无数据时显示Toast）
                    if (adapter.getItemCount() == 0) {
                        Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    } else {
                        // 已经有数据时，可以显示Snackbar提示
                        Snackbar.make(binding.getRoot(), resource.message, Snackbar.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
    }

    /**
     * 显示加载状态（可选实现）
     * 可以在这里添加加载指示器或其他加载UI元素
     */
    private void showLoading() {
        // 可以在这里添加加载指示器
        binding.progressBar.setVisibility(VISIBLE);
    }

    /**
     * 隐藏加载状态（可选实现）
     * 可以在这里隐藏加载指示器或其他加载UI元素
     */
    private void hideLoading() {
        // 可以在这里隐藏加载指示器
        binding.progressBar.setVisibility(GONE);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 防止内存泄漏
    }

    /**
     * 自定义 ViewModel 工厂类，用于创建带有参数的 CoinViewModel 实例。
     */
    private static class CoinViewModelFactory implements ViewModelProvider.Factory {
        private final CoinRepository repository;

        public CoinViewModelFactory(CoinRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CoinViewModel.class)) {
                return (T) new CoinViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
