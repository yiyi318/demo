package com.example.myapplication.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ArticleAdapter;
import com.example.myapplication.databinding.FragmentCollectBinding;
import com.example.myapplication.databinding.ToolbarBinding;
import com.example.myapplication.model.Article;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.viewmodel.profile.CollectViewModel;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;

import java.util.ArrayList;

/**
 * 收藏页面 Fragment，用于展示用户收藏的文章列表。
 * 支持查看收藏文章、取消收藏、下拉刷新和上拉加载更多功能。
 */
public class CollectFragment extends Fragment {


    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;
    private static final int HEADER_HEIGHT = 50;
    private static final int FOOTER_HEIGHT = 40;
    private static final float HEADER_TRIGGER_RATE = 0.6f;
    private static final int TOAST_DURATION = Toast.LENGTH_SHORT;
    private static final int POSITION_INVALID = -1;

    private FragmentCollectBinding binding;
    private CollectViewModel viewModel;
    private ArticleAdapter adapter;

    /**
     * 创建 CollectFragment 实例
     *
     * @return 配置好的 CollectFragment 实例
     */
    public static CollectFragment newInstance() {
        return new CollectFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCollectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        initAdapter();
        hideBottomNavigation();
        setupToolbar();
        setupRecyclerView();
        initRefreshLayout();
        setupObservers();
        viewModel.refreshData();
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CollectViewModel.class);
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        adapter = new ArticleAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(createArticleItemClickListener());
    }

    /**
     * 创建文章项点击监听器
     *
     * @return ArticleAdapter.OnItemClickListener 实例
     */
    private ArticleAdapter.OnItemClickListener createArticleItemClickListener() {
        return new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article, int position) {
                handleArticleClick(article, position);
            }

            @Override
            public void onCollectClick(Article article, int position) {
                handleUncollectClick(article, position);
            }
        };
    }

    /**
     * 处理文章点击事件
     *
     * @param article  文章对象
     * @param position 文章位置
     */
    private void handleArticleClick(Article article, int position) {
        if (article == null) return;

        if (!isValidUrl(article.getLink())) {
            showToast(getString(R.string.article_link_format_error));
            return;
        }

        openArticleInWebView(article, position);
    }

    /**
     * 处理取消收藏点击事件
     *
     * @param article  文章对象
     * @param position 文章位置
     */
    private void handleUncollectClick(Article article, int position) {
        if (article == null) return;
        int originId = article.getOrginid();
        uncollectArticle(article.getarticleId(), originId, position);
    }

    /**
     * 验证URL是否有效
     *
     * @param url 待验证的URL
     * @return true表示有效，false表示无效
     */
    private boolean isValidUrl(String url) {
        return URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url);
    }

    /**
     * 在WebView中打开文章
     *
     * @param article  文章对象
     * @param position 文章位置
     */
    private void openArticleInWebView(Article article, int position) {
        if (getActivity() == null || article == null) return;

        try {
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra("url", article.getLink());
            intent.putExtra("title", article.getTitle());
            intent.putExtra("iscollect", article.isCollect());
            intent.putExtra("articleId", article.getarticleId());
            intent.putExtra("positionId", position);
            intent.putExtra("originId", article.getOrginid());

            startActivityForResult(intent, REQUEST_CODE_ARTICLE_DETAIL);
        } catch (Exception e) {
            showToast(getString(R.string.open_article_failed));
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ARTICLE_DETAIL && resultCode == RESULT_OK && data != null) {
            handleArticleActivityResult(data);
        }
    }

    /**
     * 处理文章详情页面返回结果
     *
     * @param data 返回数据
     */
    private void handleArticleActivityResult(Intent data) {
        int position = data.getIntExtra("positionId", POSITION_INVALID);
        int originId = data.getIntExtra("originId", POSITION_INVALID);
        int articleId = data.getIntExtra("articleId", POSITION_INVALID);
        boolean isCollected = data.getBooleanExtra("is_collected", false);

        if (!isCollected && position != POSITION_INVALID) {
            uncollectArticle(articleId, originId, position);
        }
    }

    /**
     * 取消收藏文章
     *
     * @param articleId 文章ID
     * @param originId  原始文章ID
     * @param position  文章位置
     */
    private void uncollectArticle(int articleId, int originId, int position) {
        showToast(getString(R.string.uncollecting));
        viewModel.uncollectArticle(articleId,originId,position);
    }



    /**
     * 隐藏底部导航栏
     */
    private void hideBottomNavigation() {
        View bottomNav = requireActivity().findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    /**
     * 设置工具栏
     */
    private void setupToolbar() {
        if (binding == null) return;

        ToolbarBinding toolbarBinding = binding.collecttoolbar;
        toolbarBinding.toolbarTitle.setText(R.string.my_collection);
        toolbarBinding.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        toolbarBinding.qrCodeButton.setOnClickListener(v -> handleBackClick());
    }

    /**
     * 处理返回按钮点击事件
     */
    private void handleBackClick() {
        if (isAdded() && getActivity() != null) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * 设置 RecyclerView
     */
    private void setupRecyclerView() {
        if (binding == null || adapter == null) return;

        binding.rvCollect.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCollect.setAdapter(adapter);
    }

    /**
     * 初始化刷新布局
     */
    private void initRefreshLayout() {
        if (binding == null) return;
        setupRefreshHeader();
        setupRefreshFooter();
        setupRefreshListeners();
        setupRefreshParameters();
    }

    /**
     * 设置刷新头部
     */
    private void setupRefreshHeader() {
        ClassicsHeader header = new ClassicsHeader(requireContext());
        header.setPrimaryColor(Color.WHITE);
        header.setAccentColor(Color.BLUE);
        binding.refreshLayout.setRefreshHeader(header);
    }

    /**
     * 设置刷新底部
     */
    private void setupRefreshFooter() {
        ClassicsFooter footer = new ClassicsFooter(requireContext());
        footer.setAccentColor(Color.BLUE);
        binding.refreshLayout.setRefreshFooter(footer);
    }

    /**
     * 设置刷新监听器
     */
    private void setupRefreshListeners() {
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            viewModel.refreshData();
        });

        binding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            viewModel.loadMoreData();
        });
    }

    /**
     * 设置刷新参数
     */
    private void setupRefreshParameters() {
        binding.refreshLayout.setHeaderHeight(HEADER_HEIGHT);
        binding.refreshLayout.setHeaderTriggerRate(HEADER_TRIGGER_RATE);
        binding.refreshLayout.setFooterHeight(FOOTER_HEIGHT);
    }

    /**
     * 设置观察者
     */
    private void setupObservers() {
        observeArticles();
        observeErrorMessage();
        observeLoadingState();
        observeRefreshingState();
    }

    /**
     * 观察文章数据变化
     */
    private void observeArticles() {
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null && adapter != null) {
                adapter.setArticles(articles);
            }
        });
    }

    /**
     * 观察错误消息
     */
    private void observeErrorMessage() {
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showToast(error);
            }
        });
    }

    /**
     * 观察加载状态
     */
    private void observeLoadingState() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!isLoading && binding != null) {
                binding.refreshLayout.finishRefresh();
                binding.refreshLayout.finishLoadMore();
            }
        });
    }

    /**
     * 观察刷新状态
     */
    private void observeRefreshingState() {
        viewModel.getIsRefreshing().observe(getViewLifecycleOwner(), isRefreshing -> {
            if (!isRefreshing && binding != null) {
                binding.refreshLayout.finishRefresh();
            }
        });
    }

    /**
     * 显示 Toast 消息
     *
     * @param message 消息内容
     */
    private void showToast(String message) {
        if (getContext() != null && message != null) {
            Toast.makeText(getContext(), message, TOAST_DURATION).show();
        }
    }

    /**
     * 显示底部导航栏
     */
    private void showBottomNavigation() {
        View bottomNav = requireActivity().findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        showBottomNavigation();
        super.onDestroyView();
        binding = null;
    }


}
