package com.example.myapplication.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Respository.CoinRepository;
import com.example.myapplication.adapter.CoinRankAdapter;
import com.example.myapplication.databinding.FragmentCoinRankBinding;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.example.myapplication.viewmodel.profile.CoinRankViewModel;
import com.example.myapplication.viewmodel.profile.CoinRankViewModelFactory;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.header.ClassicsHeader;

/**
 * 积分排行页面 Fragment，用于展示用户积分排行榜。
 * 支持下拉刷新、上拉加载更多和用户积分信息展示功能。
 */
public class CoinRankFragment extends Fragment {
    private static final int TOAST_DURATION = Toast.LENGTH_SHORT;

    private FragmentCoinRankBinding binding;
    private CoinRankAdapter adapter;
    private CoinRankViewModel viewModel;
    private SharedViewModel sharedViewModel;


    /**
     * 创建 CoinRankFragment 实例
     *
     * @return 配置好的 CoinRankFragment 实例
     */
    public static CoinRankFragment newInstance() {
        return new CoinRankFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用 ViewBinding 初始化布局
        binding = FragmentCoinRankBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 ViewModel
        initViewModel();

        // 设置工具栏
        setupToolbar();

        // 初始化 RecyclerView
        setupRecyclerView();

        // 设置下拉刷新
        setupRefreshLayout();

        // 设置滚动监听（分页加载）
        setupScrollListener();

        // 观察数据变化
        setupDataObserver();

        // 首次加载数据（刷新）
        initLoadData();
    }

    /**
     * 初始化数据加载
     */
    private void initLoadData() {
        viewModel.refreshRankList();
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        ApiService apiService = RetrofitClient.getService();
        CoinRepository repository = new CoinRepository(apiService);
        CoinRankViewModelFactory factory = new CoinRankViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(CoinRankViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.setShouldShowBottomTab(false);
    }

    /**
     * 设置工具栏
     */
    private void setupToolbar() {
        // 设置标题和返回按钮
        binding.mycointoolbar.toolbarTitle.setText(R.string.coin_rank);
        binding.mycointoolbar.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        binding.mycointoolbar.qrCodeButton.setOnClickListener(v ->{
            sharedViewModel.setShouldShowBottomTab(true);
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 设置排名文本颜色
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.Primary);
        binding.bottomTar.tvRank.setTextColor(primaryColor);
    }

    /**
     * 设置 RecyclerView
     */
    private void setupRecyclerView() {
        // 初始化适配器
        adapter = new CoinRankAdapter();
        binding.rvCoin.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCoin.setAdapter(adapter);
        // 添加分割线装饰
        binding.rvCoin.addItemDecoration(new DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
        ));
    }

    /**
     * 设置刷新布局
     */
    private void setupRefreshLayout() {
        // 设置下拉刷新监听器
        binding.coinRefreshLayout.setOnRefreshListener(refreshLayout -> {
            viewModel.refreshRankList();
        });
        // 设置刷新头样式
        binding.coinRefreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
    }

    /**
     * 设置滚动监听器（分页加载）
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
                    viewModel.loadRankList();
                }
            }
        });
    }

    /**
     * 设置数据观察者
     */
    private void setupDataObserver() {
        // 观察排行榜数据变化
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

                    // 更新数据到适配器
                    if (resource.data != null) {
                        adapter.setData(resource.data);
                    }
                    break;

                case ERROR:
                    hideLoading();
                    binding.coinRefreshLayout.finishRefresh(false);

                    // 显示错误信息（仅在无数据时显示Toast）
                    if (adapter.getItemCount() == 0) {
                        Toast.makeText(getContext(), resource.message, TOAST_DURATION).show();
                    } else {
                        // 已经有数据时，可以显示Snackbar提示
                        Snackbar.make(binding.getRoot(), resource.message, Snackbar.LENGTH_SHORT).show();
                    }
                    break;
            }
        });

        // 根据登录状态显示或隐藏用户积分信息
        Context context = requireContext();
        if (AuthManager.isLoggedIn(context)) {
            // 显示底部导航栏
            binding.bottomTar.getRoot().setVisibility(View.VISIBLE);

            // 恢复 SmartRefreshLayout 的底部边距（如果有）
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.coinRefreshLayout.getLayoutParams();
            params.bottomMargin = (int) getResources().getDimension(R.dimen.bottom_nav_height);
            binding.coinRefreshLayout.setLayoutParams(params);

            // 观察用户积分数据变化
            viewModel.getCoinLiveData().observe(getViewLifecycleOwner(), item->{
                if (item != null) {
                    int level = item.getLevel();

                    // 根据等级设置不同颜色
                    int color;
                    if (level >= 1000) {
                        color = ContextCompat.getColor(requireContext(), R.color.gold);
                    } else if (level >= 100) {
                        color = ContextCompat.getColor(requireContext(), R.color.green);
                    } else if (level >= 10) {
                        color = ContextCompat.getColor(requireContext(), R.color.orange);
                    } else {
                        color = ContextCompat.getColor(requireContext(), R.color.blue);
                    }

                    // 更新用户积分信息显示
                    binding.bottomTar.tvLevel.setTextColor(color);
                    binding.bottomTar.tvRank.setText(item.getRank());
                    binding.bottomTar.tvUsername.setText(item.getUsername());
                    binding.bottomTar.tvCoinCount.setText(String.valueOf(item.getCoinCount()));
                    binding.bottomTar.tvLevel.setText(String.valueOf(item.getLevel()));
                }
            });
        } else {
            // 隐藏底部导航栏
            binding.bottomTar.getRoot().setVisibility(View.GONE);

            // 移除 SmartRefreshLayout 的底部边距
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.coinRefreshLayout.getLayoutParams();
            params.bottomMargin = 0;
            binding.coinRefreshLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentManager fm = getParentFragmentManager(); // 或 getFragmentManager()
        int count = fm.getBackStackEntryCount();
        Log.d("BackStack", "当前栈深度（CoinRank）: " + count);
    }

    /**
     * 显示加载状态
     */
    private void showLoading() {
        // 显示加载动画或占位视图
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏加载状态
     */
    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        // 显示底部导航栏
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        super.onDestroyView();
        binding = null;
    }
}
