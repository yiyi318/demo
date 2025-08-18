package com.example.myapplication.ui.officialaccount;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.adapter.ArticleAdapter;
import com.example.myapplication.model.Article;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.utils.AuthDialogHelper;
import com.example.myapplication.viewmodel.SharedCollectViewModel;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.example.myapplication.viewmodel.official.OfficialSecondViewModel;

/**
 * 公众号文章列表 Fragment
 * 展示指定公众号的文章列表，支持下拉刷新、分页加载、文章收藏等功能
 */
public class OfficialArticleFragment extends Fragment {
    private static final String ARG_TAB_ID = "tab_id";
    private OfficialSecondViewModel viewModel;
    private ArticleAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private SharedViewModel sharedViewModel;
    private SharedCollectViewModel collectViewModel;

    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;


    /**
     * 创建 OfficialArticleFragment 实例
     *
     * @param tabId 公众号ID
     * @return OfficialArticleFragment 实例
     */
    public static OfficialArticleFragment newInstance(int tabId) {
        OfficialArticleFragment fragment = new OfficialArticleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_ID, tabId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 创建 Fragment 视图
     *
     * @param inflater           LayoutInflater
     * @param container          ViewGroup 容器
     * @param savedInstanceState 保存的状态数据
     * @return Fragment 根视图
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 修改布局文件，添加 SwipeRefreshLayout 可以用同一个布局把
        View view = inflater.inflate(R.layout.fragment_project_article_tab, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    /**
     * 视图创建完成后初始化UI组件和数据
     *
     * @param view               Fragment 根视图
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int tabId = getArguments() != null ? getArguments().getInt(ARG_TAB_ID, 0) : 0;

        // 初始化RecyclerView
        setupRecyclerView();

        // 设置刷新监听器
        setupRefreshLayout();

        // 设置列表项点击监听器
        setupItemClickListener();

        // 添加滚动监听器实现分页加载
        setupScrollListener();

        // 初始化ViewModel
        initViewModel(tabId);

        // 观察数据变化
        observeData();
    }

    /**
     * 初始化和配置 RecyclerView
     */
    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ArticleAdapter();
        recyclerView.setAdapter(adapter);
    }

    /**
     * 设置下拉刷新监听器
     */
    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (viewModel != null) {
                viewModel.refreshArticles();
            }
        });
    }

    /**
     * 设置列表项点击监听器
     */
    private void setupItemClickListener() {
        adapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article, int position) {
                // 处理文章点击（可选）
//                handleCollectClick(article,position);
                handleArticleClick(article, position);
            }

            @Override
            public void onCollectClick(Article article, int position) {
                // 核心收藏逻辑
                handleCollectClick(article, position);
            }
        });
    }

    /**
     * 设置滚动监听器实现分页加载
     */
    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 向下滚动且不是加载中状态，检查是否需要加载更多
                if (dy > 0 && !isLoading && hasMoreData) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // 当滚动到倒数第2个item时触发加载更多
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
                        loadMoreData();
                    }
                }
            }
        });
    }

    /**
     * 处理文章点击事件，跳转到 WebView 页面
     *
     * @param article  被点击的文章
     * @param position 文章在列表中的位置
     */
    private void handleArticleClick(Article article, int position) {
        // 1. 验证URL有效性
        if (!URLUtil.isHttpUrl(article.getLink()) && !URLUtil.isHttpsUrl(article.getLink())) {
            Toast.makeText(getContext(), "文章链接格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 创建Intent
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("url", article.getLink());
        intent.putExtra("title", article.getTitle());
        intent.putExtra("iscollect", article.isCollect());
        intent.putExtra("articleId", article.getarticleId());
        intent.putExtra("positionId", position);

        startActivityForResult(intent, REQUEST_CODE_ARTICLE_DETAIL);

        // 4. 降级方案：普通跳转
    }

    /**
     * 处理从其他Activity返回的结果
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ARTICLE_DETAIL && resultCode == RESULT_OK) {
            int articleId = data.getIntExtra("positionId", -1);
            boolean isCollected = data.getBooleanExtra("is_collected", false);
            Log.d("collectapp", "onActivityResult: " + articleId);
            // 直接更新对应Item
            adapter.updateCollectState(articleId, isCollected);
        }
    }

    /**
     * 处理收藏点击事件
     *
     * @param article  被操作的文章
     * @param position 文章在列表中的位置
     */
    private void handleCollectClick(Article article, int position) {
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
        // 先立即更新UI状态（乐观更新）
        adapter.updateCollectState(position, !isCollect);

        LiveData<Boolean> operationResult = isCollect ?
                collectViewModel.uncollectArticle(article.getarticleId()) :
                collectViewModel.collectArticle(article.getarticleId());

        operationResult.observe(getViewLifecycleOwner(), success -> {
            if (success == null || !success) {
                // 操作失败，回滚UI状态
                adapter.updateCollectState(position, isCollect);
                String errorMsg = isCollect ? "取消收藏失败" : "收藏失败";
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 初始化 ViewModel
     *
     * @param tabId 公众号ID
     */
    private void initViewModel(int tabId) {
        Repository repository = new Repository(RetrofitClient.getService());

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new OfficialSecondViewModel(repository, tabId);
            }
        }).get(OfficialSecondViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        collectViewModel=new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
    }

    /**
     * 观察 ViewModel 中的数据变化
     */
    private void observeData() {
        // 观察文章数据
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                adapter.setArticles(articles);
            }
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            this.isLoading = isLoading;
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // 观察错误信息
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // 观察是否还有更多数据
        viewModel.getHasMore().observe(getViewLifecycleOwner(), hasMore -> {
            if (hasMore != null) { // 增加空值判断
                this.hasMoreData = hasMore;
            }
        });
    }

    /**
     * 加载更多数据
     */
    private void loadMoreData() {
        if (viewModel != null && !isLoading && hasMoreData) {
            viewModel.loadMoreArticles();
        }
    }

    /**
     * Fragment onResume 生命周期方法
     * 当Fragment可见时刷新数据（如果列表为空）
     */
    @Override
    public void onResume() {
        super.onResume();
        // 确保Fragment可见时刷新数据
        if (viewModel != null && adapter.getItemCount() == 0) {
            viewModel.refreshArticles();
        }
    }
}
