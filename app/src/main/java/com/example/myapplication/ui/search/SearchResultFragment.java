package com.example.myapplication.ui.search;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.adapter.ArticleAdapter;
import com.example.myapplication.model.Article;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.viewmodel.SharedCollectViewModel;
import com.example.myapplication.viewmodel.search.SearchViewModel;

/**
 * 搜索结果页面 Fragment，用于展示特定关键词的搜索结果。
 * 支持文章列表展示、收藏功能、加载更多等。
 */
public class SearchResultFragment extends Fragment {
    private static final String ARG_SEARCH_QUERY = "SEARCH_QUERY";
    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;

    private SharedCollectViewModel collectViewModel;

    private RecyclerView recyclerView;
    private SearchViewModel viewModel;
    private ArticleAdapter adapter;

    /**
     * 创建 SearchResultFragment 实例并传递搜索关键词
     *
     * @param query 搜索关键词
     * @return 配置好的 SearchResultFragment 实例
     */
    public static SearchResultFragment newInstance(String query) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModels();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
               // 初始化视图组件
        initViews(view);
        // 设置RecyclerView配置
        setupRecyclerView();
        // 设置工具栏
        setupToolbar(view);
        // 设置滚动监听器
        setupScrollListener();
        // 观察ViewModel数据变化
        observeViewModelData();
        // 执行初始搜索操作
        performInitialSearch();

    }

    /**
     * 初始化 ViewModels
     */
    private void initViewModels() {
        Repository repository = new Repository(RetrofitClient.getService());
        collectViewModel = new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
        viewModel = new ViewModelProvider(this, new SearchViewModelFactory(repository))
                .get(SearchViewModel.class);
    }

    /**
     * 初始化视图组件
     *
     * @param view Fragment 根视图
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        if (recyclerView == null) {
            Toast.makeText(requireContext(), "界面初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置 RecyclerView 及其适配器
     */
    private void setupRecyclerView() {
        if (recyclerView == null) return;

        adapter = new ArticleAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article, int position) {
                handleArticleClick(article, position);
            }

            @Override
            public void onCollectClick(Article article, int position) {
                handleCollectClick(article, position);
            }
        });
    }

    /**
     * 设置工具栏，包括标题和返回按钮
     *
     * @param view Fragment 根视图
     */
    private void setupToolbar(View view) {
        View toolbar = view.findViewById(R.id.searchresulttoolbar);
        if (toolbar == null) return;

        TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (titleView != null) {
            String query = getArguments() != null ? getArguments().getString(ARG_SEARCH_QUERY, "") : "";
            titleView.setText(query);
        }

        ImageButton backButton = toolbar.findViewById(R.id.qr_code_button);
        if (backButton != null) {
            backButton.setImageResource(R.drawable.ic_arrow_left);
            backButton.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    getActivity().findViewById(R.id.nestedScrollView).setVisibility(View.VISIBLE);
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    /**
     * 设置滚动监听器，用于实现加载更多功能
     */
    private void setupScrollListener() {
        if (recyclerView == null) return;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && viewModel.hasMoreResults()) {
                    viewModel.loadMoreResults();
                }
            }
        });
    }

    /**
     * 观察 ViewModel 数据变化
     */
    private void observeViewModelData() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), this::updateSearchResults);
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), this::showErrorMessage);
    }

    /**
     * 更新搜索结果列表
     *
     * @param articles 搜索结果文章列表
     */
    private void updateSearchResults(java.util.List<Article> articles) {
        if (adapter != null) {
            adapter.setArticles(articles);
        }
    }

    /**
     * 显示错误信息
     *
     * @param errorMsg 错误信息
     */
    private void showErrorMessage(String errorMsg) {
        if (errorMsg != null && !errorMsg.isEmpty()) {
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 执行初始搜索
     */
    private void performInitialSearch() {
        String query = getArguments() != null ? getArguments().getString(ARG_SEARCH_QUERY, "") : "";
        viewModel.newSearch(query);
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

        try {
            startActivityForResult(intent, REQUEST_CODE_ARTICLE_DETAIL);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "打开文章失败", Toast.LENGTH_SHORT).show();
        }
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
        if (requestCode == REQUEST_CODE_ARTICLE_DETAIL && resultCode == RESULT_OK && data != null) {
            int articleId = data.getIntExtra("positionId", -1);
            boolean isCollected = data.getBooleanExtra("is_collected", false);
            if (adapter != null) {
                adapter.updateCollectState(articleId, isCollected);
            }
        }
    }

    /**
     * 处理文章收藏点击事件
     *
     * @param article  被点击的文章对象
     * @param position 文章在列表中的位置
     */
    private void handleCollectClick(Article article, int position) {
        if (!AuthManager.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCollect = article.isCollect();
        if (adapter != null) {
            adapter.updateCollectState(position, !isCollect);
        }

        LiveData<Boolean> operationResult = isCollect ?
                collectViewModel.uncollectArticle(article.getarticleId()) :
                collectViewModel.collectArticle(article.getarticleId());

        operationResult.observe(getViewLifecycleOwner(), success -> {
            if (success == null || !success) {
                if (adapter != null) {
                    adapter.updateCollectState(position, isCollect);
                }
                String errorMsg = isCollect ? "取消收藏失败" : "收藏失败";
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 搜索 ViewModel 工厂类，用于创建 SearchViewModel 实例
     */
    private static class SearchViewModelFactory implements ViewModelProvider.Factory {
        private final Repository repository;

        public SearchViewModelFactory(Repository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SearchViewModel.class)) {
                return modelClass.cast(new SearchViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
