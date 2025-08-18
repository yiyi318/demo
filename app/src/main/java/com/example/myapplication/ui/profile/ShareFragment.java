package com.example.myapplication.ui.profile;

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

import com.example.myapplication.R;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.adapter.ArticleAdapter;
import com.example.myapplication.databinding.FragmentShareBinding;
import com.example.myapplication.model.Article;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.utils.AuthDialogHelper;
import com.example.myapplication.viewmodel.SharedCollectViewModel;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.example.myapplication.viewmodel.profile.ShareViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的分享页面 Fragment，用于展示用户分享的文章列表。
 * 支持文章浏览、收藏、删除等功能。
 */
public class ShareFragment extends Fragment {
    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;
    private static final String BACKSTACK_TAG_SHARE_ARTICLE = "share_article";

    private FragmentShareBinding binding;
    private ShareViewModel viewModel;
    private SharedCollectViewModel collectViewModel;
    private ArticleAdapter adapter;
    private SharedViewModel sharedViewModel;

    /**
     * 创建 ShareFragment 实例
     *
     * @return 配置好的 ShareFragment 实例
     */
    public static ShareFragment newInstance() {
        return new ShareFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShareBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModels();

        initAdapter();
                // 设置工具栏
        setupToolbar();
        // 设置RecyclerView
        setupRecyclerView();


        // 设置下拉刷新布局
        setupRefreshLayout();
        // 观察ViewModel数据变化
        observeViewModelData();
        // 加载初始数据
        loadInitialData();

    }

    /**
     * 初始化 ViewModels
     */
    private void initViewModels() {
        Repository repository = new Repository(RetrofitClient.getService());
        viewModel = new ViewModelProvider(this, new ShareViewModelFactory(repository))
                .get(ShareViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        collectViewModel=new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        adapter = new ArticleAdapter(new ArrayList<>());
    }

    /**
     * 设置工具栏
     */
    private void setupToolbar() {
        if (binding == null) return;

        binding.sharetoolbar.toolbarTitle.setText(R.string.my_share);
        binding.sharetoolbar.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        binding.sharetoolbar.qrCodeButton.setOnClickListener(v -> handleBackClick());
        binding.sharetoolbar.btnSave.setImageResource(R.drawable.ic_add);
        binding.sharetoolbar.btnSave.setOnClickListener(v -> navigateToShareArticle());
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
     * 跳转到分享文章页面
     */
    private void navigateToShareArticle() {
        try {
            ShareArticleFragment fragment = new ShareArticleFragment();
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(BACKSTACK_TAG_SHARE_ARTICLE)
                        .commit();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "页面跳转失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置 RecyclerView 及其适配器
     */
    private void setupRecyclerView() {
        if (binding == null) return;

        binding.rvCollect.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCollect.setAdapter(adapter);
        adapter.setOnItemClickListener(createArticleClickListener());
    }

    /**
     * 创建文章点击监听器
     *
     * @return ArticleAdapter.OnItemClickListener 实例
     */
    private ArticleAdapter.OnItemClickListener createArticleClickListener() {
        return new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article, int position) {
                handleArticleClick(article, position);
            }

            @Override
            public void onCollectClick(Article article, int position) {
                handleCollectClick(article, position);
            }

            @Override
            public void onLongClick(Article article) {
                handleArticleLongClick(article);
            }
        };
    }


    /**
     * 设置刷新布局
     */
    private void setupRefreshLayout() {
        if (binding == null) return;

        binding.refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));

        binding.refreshLayout.setOnRefreshListener(refreshLayout -> viewModel.refreshArticles());
        binding.refreshLayout.setOnLoadMoreListener(refreshLayout -> viewModel.loadMoreArticles());
    }

    /**
     * 观察 ViewModel 数据变化
     */
    private void observeViewModelData() {
        viewModel.getArticles().observe(getViewLifecycleOwner(), this::updateArticleList);
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), this::showErrorMessage);
    }

    /**
     * 更新文章列表数据
     *
     * @param articles 文章列表数据
     */
    private void updateArticleList(List<Article> articles) {
        if (adapter != null) {
            adapter.setArticles(articles);
        }

        if (binding != null) {
            binding.refreshLayout.finishRefresh();
            binding.refreshLayout.finishLoadMore();
        }
    }


    /**
     * 显示错误信息
     *
     * @param error 错误信息
     */
    private void showErrorMessage(String error) {
        if (error != null && getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        viewModel.refreshArticles();
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
     * 处理文章长按事件，弹出删除确认对话框
     *
     * @param article 被长按的文章对象
     */
    private void handleArticleLongClick(Article article) {
        int id = article.getarticleId();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除文章")
                .setMessage("确定要删除这篇文章吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteArticle(id))
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     */
    private void deleteArticle(int articleId) {
        viewModel.postDeleteShareArticle(articleId).observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                viewModel.refreshArticles();
            } else {
                Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 分享 ViewModel 工厂类，用于创建 ShareViewModel 实例
     */
    private static class ShareViewModelFactory implements ViewModelProvider.Factory {
        private final Repository repository;

        public ShareViewModelFactory(Repository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ShareViewModel.class)) {
                return modelClass.cast(new ShareViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
