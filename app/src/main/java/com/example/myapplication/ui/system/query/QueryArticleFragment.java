package com.example.myapplication.ui.system.query;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.adapter.QueryArticleAdapter;
import com.example.myapplication.databinding.FragmentQueryArticleBinding;
import com.example.myapplication.model.QueryArticle;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.utils.AuthDialogHelper;
import com.example.myapplication.viewmodel.SharedCollectViewModel;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.example.myapplication.viewmodel.system.QueryArticleViewModel;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

/**
 * 查询文章页面 Fragment，用于展示搜索结果或特定查询条件下的文章列表。
 * 支持下拉刷新、上拉加载更多、文章点击跳转、收藏功能等。
 */
public class QueryArticleFragment extends Fragment {
    private FragmentQueryArticleBinding binding;
    private QueryArticleViewModel queryVM;
    private SharedCollectViewModel collectViewModel;
    private QueryArticleAdapter queryAdapter;
    private SharedViewModel sharedViewModel;

    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;

    /**
     * 创建 QueryArticleFragment 实例
     *
     * @return 配置好的 QueryArticleFragment 实例
     */
    public static QueryArticleFragment newInstance() {
        return new QueryArticleFragment();
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
        binding = FragmentQueryArticleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupRefreshLayout();
        observeViewModelData();
        loadInitialData();
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        SystemRepository repository = new SystemRepository(RetrofitClient.getService());
        queryVM = new ViewModelProvider(this, new QueryViewModelFactory(repository))
                .get(QueryArticleViewModel.class);
        collectViewModel=new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    /**
     * 设置 RecyclerView 及其适配器
     */
    private void setupRecyclerView() {
        queryAdapter = new QueryArticleAdapter();
        initRecyclerView(binding.recyclerView, queryAdapter);
        queryAdapter.setOnItemClickListener(new QueryArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(QueryArticle article, int position) {
                handleArticleClick(article, position);
            }

            @Override
            public void onCollectClick(QueryArticle article, int position) {
                handleCollectClick(article, position);
            }
        });
    }

    /**
     * 通用的 RecyclerView 初始化方法
     *
     * @param recyclerView 目标 RecyclerView
     * @param adapter      适配器
     */
    protected void initRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    /**
     * 设置刷新布局，包括下拉刷新和上拉加载更多
     */
    private void setupRefreshLayout() {
        binding.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        binding.refreshLayout.setOnRefreshListener(this::handleRefresh);
        binding.refreshLayout.setOnLoadMoreListener(this::handleLoadMore);
    }

    /**
     * 观察 ViewModel 数据变化
     */
    private void observeViewModelData() {
        queryVM.getQueryArticles().observe(getViewLifecycleOwner(), this::updateArticleList);
    }

    /**
     * 更新文章列表数据
     *
     * @param articles 文章列表数据
     */
    private void updateArticleList(java.util.List<QueryArticle> articles) {
        if (articles != null) {
            queryAdapter.setArticles(articles);
        } else {
            Toast.makeText(requireContext(), "暂无数据", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        queryVM.refreshQueryArticles();
    }

    /**
     * 处理下拉刷新事件
     *
     * @param refreshLayout 刷新布局
     */
    private void handleRefresh(RefreshLayout refreshLayout) {
        queryVM.refreshQueryArticles();
        queryVM.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null || !isLoading) {
                if (queryVM.hasMore()) {
                    refreshLayout.finishRefresh();
                } else {
                    refreshLayout.finishRefreshWithNoMoreData();
                }
            }
        });
    }

    /**
     * 处理上拉加载更多事件
     *
     * @param refreshLayout 刷新布局
     */
    private void handleLoadMore(RefreshLayout refreshLayout) {
        if (Boolean.TRUE.equals(queryVM.getIsLoading().getValue())) {
            refreshLayout.finishLoadMore();
            return;
        }

        if (queryVM.hasMore()) {
            queryVM.loadMoreQueryArticles();
            queryVM.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                if (isLoading == null || !isLoading) {
                    if (queryVM.hasMore()) {
                        refreshLayout.finishLoadMore();
                    } else {
                        refreshLayout.finishLoadMoreWithNoMoreData();
                    }
                }
            });
        } else {
            refreshLayout.finishLoadMoreWithNoMoreData();
        }
    }

    /**
     * 处理文章点击事件，跳转到 WebViewActivity 显示文章内容
     *
     * @param article  被点击的文章对象
     * @param position 文章在列表中的位置
     */
    private void handleArticleClick(QueryArticle article, int position) {
        if (!URLUtil.isHttpUrl(article.getLink()) && !URLUtil.isHttpsUrl(article.getLink())) {
            Toast.makeText(getContext(), "文章链接格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("url", article.getLink());
        intent.putExtra("title", article.getTitle());
        intent.putExtra("iscollect", article.isCollect());
        intent.putExtra("articleId", article.getId());
        intent.putExtra("positionId", position);

        startActivityForResult(intent, REQUEST_CODE_ARTICLE_DETAIL);
    }

    /**
     * 处理从其他页面返回的结果，更新文章的收藏状态
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ARTICLE_DETAIL && data != null) {
            int positionId = data.getIntExtra("positionId", -1);
            boolean isCollected = data.getBooleanExtra("is_collected", false);
            queryAdapter.updateCollectState(positionId, isCollected);
        }
    }

    /**
     * 处理文章收藏点击事件
     *
     * @param article  被点击的文章对象
     * @param position 文章在列表中的位置
     */
    private void handleCollectClick(QueryArticle article, int position) {
        if (!AuthManager.isLoggedIn(requireContext())) {
            AuthDialogHelper.showLoginDialog(
                    requireContext(),
                    requireActivity().getSupportFragmentManager(),
                    () -> {
                        sharedViewModel.setLoggedIn(true);
                        handleCollectClick(article, position);
                    }
            );
            return;
        }

        boolean isCollect = article.isCollect();
        queryAdapter.updateCollectState(position, !isCollect);

        LiveData<Boolean> operation = isCollect ?
                collectViewModel.uncollectArticle(article.getId()) :
                collectViewModel.collectArticle(article.getId());

        operation.observe(getViewLifecycleOwner(), success -> {
            if (success == null || !success) {
                queryAdapter.updateCollectState(position, isCollect);
                Toast.makeText(requireContext(),
                        isCollect ? "取消收藏失败" : "收藏失败",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 查询文章 ViewModel 工厂类，用于创建 QueryArticleViewModel 实例
     */
    private static class QueryViewModelFactory implements ViewModelProvider.Factory {
        private final SystemRepository repository;

        public QueryViewModelFactory(SystemRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(QueryArticleViewModel.class)) {
                return modelClass.cast(new QueryArticleViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
