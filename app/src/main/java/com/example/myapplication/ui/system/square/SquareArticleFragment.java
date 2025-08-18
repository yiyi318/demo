package com.example.myapplication.ui.system.square;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.adapter.ArticleAdapter;
import com.example.myapplication.databinding.FragmentSquareArticleBinding;
import com.example.myapplication.model.Article;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.utils.AuthDialogHelper;
import com.example.myapplication.viewmodel.SharedCollectViewModel;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.example.myapplication.viewmodel.system.SquareViewModel;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

/**
 * 广场文章页面 Fragment，用于展示广场推荐的文章列表。
 * 支持下拉刷新、上拉加载更多、文章点击跳转、收藏功能等。
 */
public class SquareArticleFragment extends Fragment {
    private FragmentSquareArticleBinding binding;
    private SquareViewModel squareVM;
    private ArticleAdapter squareAdapter;
    private SharedViewModel sharedViewModel;
    private SharedCollectViewModel collectViewModel;

    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1002;

    /**
     * 创建 SquareArticleFragment 实例
     *
     * @return 配置好的 SquareArticleFragment 实例
     */
    public static SquareArticleFragment newInstance() {
        return new SquareArticleFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSquareArticleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
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
        squareVM = new ViewModelProvider(this, new SquareViewModelFactory(repository))
                .get(SquareViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        collectViewModel=new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
    }



    /**
     * 设置 RecyclerView 及其适配器
     */
    private void setupRecyclerView() {
        squareAdapter = new ArticleAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(squareAdapter );
        squareAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
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
        squareVM.getArticles().observe(getViewLifecycleOwner(), this::updateArticleList);
    }

    /**
     * 更新文章列表数据
     *
     * @param articles 文章列表数据
     */
    private void updateArticleList(java.util.List<Article> articles) {
        if (articles != null) {
            squareAdapter.setArticles(articles);
        }
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        squareVM.refreshArticles();
    }

    /**
     * 处理下拉刷新事件
     *
     * @param refreshLayout 刷新布局
     */
    private void handleRefresh(RefreshLayout refreshLayout) {
        squareVM.refreshArticles();
        squareVM.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null || !isLoading) {
                if (squareVM.hasMore()) {
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
        if (Boolean.TRUE.equals(squareVM.getIsLoading().getValue())) {
            refreshLayout.finishLoadMore();
            return;
        }

        if (squareVM.hasMore()) {
            squareVM.loadMoreArticles();
            squareVM.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                if (isLoading == null || !isLoading) {
                    if (squareVM.hasMore()) {
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
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ARTICLE_DETAIL && data != null) {
            int articleId = data.getIntExtra("positionId", -1);
            boolean isCollected = data.getBooleanExtra("is_collected", false);
            squareAdapter.updateCollectState(articleId, isCollected);
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
        squareAdapter.updateCollectState(position, !isCollect);

        LiveData<Boolean> operationResult = isCollect ?
                collectViewModel.uncollectArticle(article.getarticleId()) :
                collectViewModel.collectArticle(article.getarticleId());

        operationResult.observe(getViewLifecycleOwner(), success -> {
            if (success == null || !success) {
                squareAdapter.updateCollectState(position, isCollect);
                String errorMsg = isCollect ? "取消收藏失败" : "收藏失败";
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 通用的 Fragment 跳转方法
     *
     * @param fragment  目标 Fragment
     * @param backStack 回退栈名称
     */
    protected void navigateToFragment(Fragment fragment, String backStack) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(backStack)
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 广场 ViewModel 工厂类，用于创建 SquareViewModel 实例
     */
    private static class SquareViewModelFactory implements ViewModelProvider.Factory {
        private final SystemRepository repository;

        public SquareViewModelFactory(SystemRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SquareViewModel.class)) {
                return modelClass.cast(new SquareViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
