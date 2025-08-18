package com.example.myapplication.ui.system.system;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
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
import com.example.myapplication.viewmodel.system.ArticleListViewModel;

/**
 * 文章列表 Fragment，用于展示某个章节下的所有文章。
 * 支持下拉刷新、上拉加载更多、文章点击跳转、收藏功能等。
 */
public class ArticleListFragment extends Fragment {

    /**
     * 请求码，用于从文章详情页返回时更新收藏状态
     */
    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;

    /**
     * 文章适配器，用于 RecyclerView 显示文章列表
     */
    private ArticleAdapter adapter;

    /**
     * 当前显示的章节 ID，默认为 -1 表示未设置
     */
    private int chapterId = -1;

    /**
     * 用于显示文章列表的 RecyclerView 控件
     */
    private RecyclerView recyclerView;

    /**
     * 下拉刷新控件，用于刷新文章列表
     */
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * 文章列表的 ViewModel，负责管理文章数据
     */
    private ArticleListViewModel viewModel;
    private SharedCollectViewModel collectViewModel;

    /**
     * 共享 ViewModel，用于跨 Fragment 通信（如登录状态）
     */
    private SharedViewModel sharedViewModel;

    /**
     * 创建一个新的 ArticleListFragment 实例，并传入章节 ID
     *
     * @param chapterId 章节 ID，用于加载该章节下的文章列表
     * @return 返回配置好的 ArticleListFragment 实例
     */
    public static ArticleListFragment newInstance(int chapterId) {
        ArticleListFragment fragment = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putInt("chapter_id", chapterId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Fragment 创建时调用，初始化参数和 ViewModel
     *
     * @param savedInstanceState 保存的 Fragment 状态
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chapterId = getArguments().getInt("chapter_id", -1);
        }
        initViewModel();
    }

    /**
     * 创建 Fragment 的视图布局
     *
     * @param inflater           布局加载器
     * @param container          父容器
     * @param savedInstanceState 保存的 Fragment 状态
     * @return 返回加载的布局视图
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 只负责加载布局
        return inflater.inflate(R.layout.fragment_article_list, container, false);
    }

    /**
     * 视图创建完成后调用，初始化 UI 组件并设置监听器
     *
     * @param view               Fragment 的根视图
     * @param savedInstanceState 保存的 Fragment 状态
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 统一在这里初始化所有 UI 组件
        initViews(view);

        // 初始化 RecyclerView
        setupRecyclerView();

        // 设置适配器和点击监听
        setupAdapter();

        // 设置刷新监听
        setupSwipeRefresh();

        // 设置滚动监听
        setupScrollListener();

        // 观察数据变化
        observeViewModel();

        // 加载初始数据
        initLoadData();
    }

    /**
     * 初始化并加载文章数据
     */
    private void initLoadData() {
        viewModel.refreshArticles();
    }

    /**
     * 初始化 ViewModel，包括 ArticleListViewModel 和 SharedViewModel
     */
    private void initViewModel() {
        Repository repository = new Repository(RetrofitClient.getService());
        viewModel = new ViewModelProvider(this, new ArticleListViewModelFactory(repository)).get(ArticleListViewModel.class);
        viewModel.setChapterId(chapterId);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        collectViewModel=new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
    }

    /**
     * 初始化 Fragment 中的 UI 控件
     *
     * @param view Fragment 的根视图
     */
    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);

        if (recyclerView == null) {
            throw new IllegalStateException("RecyclerView not found in layout");
        }

        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.color_accent)
        );
    }

    /**
     * 配置 RecyclerView 的布局管理器
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * 设置 RecyclerView 的适配器，并绑定点击事件
     */
    private void setupAdapter() {
        adapter = new ArticleAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article, int position) {
                handleArticleClick(article, position);
            }

            @Override
            public void onCollectClick(Article article, int position) {
                Log.d("COLLECT", "收到收藏点击事件，文章ID: " + article.getarticleId() + "文章状态" + article.isCollect());
                handleCollectClick(article, position);
            }
        });
    }

    /**
     * 设置下拉刷新监听器，刷新文章列表
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshArticles());
    }

    /**
     * 设置 RecyclerView 滚动监听器，用于上拉加载更多
     */
    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(!recyclerView.canScrollVertically(-1));

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if (canLoadMore() && lastVisibleItem >= totalItemCount - 1) {
                    viewModel.loadMoreArticles();
                }
            }
        });
    }

    /**
     * 观察 ViewModel 中的数据变化并更新 UI
     */
    private void observeViewModel() {
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                adapter.setArticles(articles);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                swipeRefreshLayout.setRefreshing(isLoading);
                if (!isLoading) {
                    Boolean hasMore = viewModel.getHasMore().getValue();
                    adapter.showFooter(hasMore != null && !hasMore);
                }
            }
        });

        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 判断是否可以加载更多文章
     *
     * @return true 表示可以加载更多，false 表示不能加载
     */
    private boolean canLoadMore() {
        Boolean hasMore = viewModel.getHasMore().getValue();
        Boolean isLoading = viewModel.getIsLoading().getValue();
        return chapterId != -1 && (isLoading == null || !isLoading) && (hasMore == null || hasMore);
    }

    /**
     * 处理文章点击事件，跳转到 WebViewActivity 显示文章内容
     *
     * @param article  被点击的文章对象
     * @param position 文章在列表中的位置
     */
    private void handleArticleClick(Article article, int position) {
        if (!URLUtil.isHttpUrl(article.getLink()) && !URLUtil.isHttpsUrl(article.getLink())) {
            Toast.makeText(getContext(), "文章链接格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("url", article.getLink());
        intent.putExtra("title", article.getTitle());
        intent.putExtra("iscollect", article.isCollect());
        intent.putExtra("articleId", article.getarticleId());
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
        if (requestCode == REQUEST_CODE_ARTICLE_DETAIL && resultCode == RESULT_OK) {
            int articleId = data.getIntExtra("positionId", -1);
            boolean isCollected = data.getBooleanExtra("is_collected", false);
            Log.d("collectapp", "onActivityResult: " + articleId);
            adapter.updateCollectState(articleId, isCollected);
        }
    }

    /**
     * 处理文章收藏点击事件，如果未登录则弹出登录对话框
     *
     * @param article  被点击的文章对象
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
     * 销毁视图时调用，释放资源
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.setAdapter(null);
    }

    /**
     * 自定义 ViewModel 工厂类，用于创建 ArticleListViewModel 实例
     */
    static class ArticleListViewModelFactory implements ViewModelProvider.Factory {
        private final Repository repository;

        ArticleListViewModelFactory(Repository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ArticleListViewModel(repository);
        }
    }
}
