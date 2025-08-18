package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Repository.BannerRepository;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.adapter.ArticleAdapter;
import com.example.myapplication.adapter.BannerAdapter;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.model.Article;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.ui.search.SearchActivity;
import com.example.myapplication.utils.AuthDialogHelper;
import com.example.myapplication.utils.qrcode.QrCodeScanner;
import com.example.myapplication.viewmodel.SharedCollectViewModel;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.example.myapplication.viewmodel.home.HomeViewModel;
import com.example.myapplication.viewmodel.home.HomeViewModelFactory;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;

import java.util.ArrayList;



public class HomeFragment extends Fragment {
    //这是一个绑定视图的binding
    private FragmentHomeBinding binding;
    //这是viewmodel用来绑定数据
    private HomeViewModel viewModel;
    //adapter 用来绑定数据和UI
    private ArticleAdapter articleAdapter;
    private BannerAdapter bannerAdapter;
    //指示器
    private ImageView[] dots;  // 用数组保存点的引用
    private int dotsCount;
    //定义了一个处理消息可以在thread中运行的函数
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    //定义了一个可以运行的函数
    private Runnable autoScrollRunnable;
    private SharedViewModel sharedViewModel;

    private SharedCollectViewModel collectViewModel;
    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;

    private boolean bannerInitialized=false;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化ViewModel
        initViewModel();
        //绑定adapter吗
        initViews();
        //设置刷新
        initRefreshLayout();
        //设置观察数据
        observeData();
        //加载数据
        loadInitialData();

    }

    private void initViewModel() {
        Repository repository = new Repository(RetrofitClient.getService());
        BannerRepository bannerRepository = new BannerRepository();
        HomeViewModelFactory factory = new HomeViewModelFactory(repository, bannerRepository);
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        collectViewModel = new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
    }

    private void observeData() {
        // java lambda表达式不能修改普通局部变量，数据是引用
        final boolean[] isRefreshing = {false};

        // 文章数据
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                articleAdapter.setArticles(articles);
            }
        });

        // Banner数据
        viewModel.getBanners().observe(getViewLifecycleOwner(), banners -> {
            if (banners != null && !banners.isEmpty()) {
                bannerAdapter.setBanners(banners);
                if (!bannerInitialized) {
                    initIndicators(banners.size());
                    startAutoScroll();
                    bannerInitialized = true;
                }
            }
        });

        // 观察合并加载状态  下拉刷新
        viewModel.getIsLoadingCombined().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;

            if (isLoading) {//如果正在加载
                binding.progressBar.setVisibility(View.VISIBLE); // 显示加载
                if (!isRefreshing[0]) {//如果没有正在刷新
                    isRefreshing[0] = true;//设置为正在刷新
                    binding.refreshLayout.autoRefresh();//开启 SmartRefreshLayout 的下拉刷新动画
                }
            } else {//如果加载结束
                binding.progressBar.setVisibility(View.GONE); // 显示加载结束
                if (isRefreshing[0]) {//还在刷新
                    isRefreshing[0] = false;//设置为不刷新
                    boolean success = viewModel.getErrorMessage().getValue() == null;//只要没有错误信息（errorMessage == null），就算成功
                    binding.refreshLayout.finishRefresh(success);//判断刷新结束
                }
            }
        });
//判断文章是否加载结束  上拉加载
        viewModel.getisLoadingArticles().observe(getViewLifecycleOwner(), isLoadingArticles -> {
            if (isLoadingArticles == null) return;

            if (!isLoadingArticles) {//如果加载结束
                // 加载完成时结束动画
                boolean success = viewModel.getErrorMessage().getValue() == null;
                binding.refreshLayout.finishLoadMore(success); // 简化处理，具体逻辑可由ViewModel控制
            }
        });

        // 错误处理（优化为只处理非空错误消息）
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showErrorState(); // 有错误就显示错误页
                if (isRefreshing[0]) {//如果正在刷新
                    binding.refreshLayout.finishRefresh(false);
                    isRefreshing[0] = false; // 防止重复finish
                }
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //初始化组件
    private void initViews() {
        initToolbar();
        //加载banner
        initBanner();
        // 文章列表
        initArticleList();
        //绑定重试按钮
        binding.btnRetry.setOnClickListener(v -> {
            retryLoadData(); // 点击时重新加载数据
        });

    }

    private void initToolbar() {
        binding.hometoolbar.qrCodeButton.setImageResource(R.drawable.qr_code_icon);
        binding.hometoolbar.toolbarTitle.setText("首页");
        binding.hometoolbar.btnSave.setImageResource(R.drawable.ic_search);
        binding.hometoolbar.qrCodeButton.setOnClickListener(v -> {
            if (!QrCodeScanner.isCameraAvailable(requireContext())) {
                Toast.makeText(requireContext(), "未检测到可用摄像头", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                QrCodeScanner.startScan(requireActivity(), (QrCodeScanner.ScanResultCallback) requireActivity());
            } catch (Exception e) {
                Toast.makeText(requireContext(), "启动扫码失败", Toast.LENGTH_SHORT).show();
            }
        });
        binding.hometoolbar.btnSave.setOnClickListener(v -> {
            goToSearchActivity();
            }
        );
    }

    private void goToSearchActivity() {
        //这里intent进行通信
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        startActivity(intent);
        Log.d("MainActivity", "已跳转到 SearchActivity");
    }


    //加载文章 绑定视图UI
    void initArticleList() {
        binding.articleList.setLayoutManager(new LinearLayoutManager(requireContext()));//	告诉 RecyclerView 用什么方式排列列表
        articleAdapter = new ArticleAdapter(new ArrayList<>());
        binding.articleList.setAdapter(articleAdapter);

        // 新增：设置收藏点击监听
        articleAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article,int position) {
                // 原有文章点击逻辑
                handleArticleClick(article, position);
            }

            @Override
            public void onCollectClick(Article article, int position) {
                handleCollectClick(article, position);
            }
        });
    }
    //加载banner 绑定视图UI
    private void initBanner() {
        // 1. 初始化Adapter
        bannerAdapter = new BannerAdapter();
        binding.bannerPager.setAdapter(bannerAdapter);
        binding.bannerPager.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.bannerPager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        updateIndicator(position % dotsCount); // 如果轮播无限循环，取模
                    }
                }
            }
        });

        // 2. 添加精准停靠（模拟ViewPager效果）
        new PagerSnapHelper().attachToRecyclerView(binding.bannerPager);
    }

    private void retryLoadData() {
        binding.layoutError.setVisibility(View.GONE); // 隐藏错误页
        binding.progressBar.setVisibility(View.VISIBLE); // 显示加载中
        loadInitialData();
        binding.articleList.setVisibility(View.VISIBLE);
        binding.bannerPager.setVisibility(View.VISIBLE);
    }




//错误页展示
    private void showErrorState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.articleList.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.bannerPager.setVisibility(View.GONE);
        binding.indicatorLayout.setVisibility(View.GONE);
    }




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
        intent.putExtra("iscollect",article.isCollect());
        intent.putExtra("articleId",article.getarticleId());
        intent.putExtra("positionId",position);

        startActivityForResult(intent, REQUEST_CODE_ARTICLE_DETAIL);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_ARTICLE_DETAIL && resultCode == RESULT_OK) {
            int articleId = data.getIntExtra("positionId", -1);
            boolean isCollected = data.getBooleanExtra("is_collected", false);
            Log.d("collectapp", "onActivityResult: "+articleId);
            // 直接更新对应Item
            articleAdapter.updateCollectState(articleId, isCollected);
        }
    }



    // 新增方法：处理收藏点击
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

        articleAdapter.updateCollectState(position, !isCollect);
        //根据文章的收藏状态来执行操作，如果iscollect为true,执行取消收藏，如果为false,执行收藏逻辑
        LiveData<Boolean> operationResult = isCollect ?
                collectViewModel.uncollectArticle(article.getarticleId()) :
                collectViewModel.collectArticle(article.getarticleId());
        //观察
        operationResult.observe(getViewLifecycleOwner(), success -> {
            if (success == null || !success) {
                // 操作失败，回滚UI状态
                articleAdapter.updateCollectState(position, isCollect);
                String errorMsg = isCollect ? "取消收藏失败" : "收藏失败";
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 初始化指示器（简单圆点）
    private void initIndicators(int count) {
        binding.indicatorLayout.removeAllViews();
        dotsCount = count;
        dots = new ImageView[dotsCount];

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(requireContext());
            dot.setImageResource(R.drawable.selector_dot);//
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            binding.indicatorLayout.addView(dot, params);
            dots[i] = dot;
        }
    }

    // 轮播时调用，更新点的状态
    private void updateIndicator(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setSelected(i == position);
        }
    }



// 自动轮播逻辑
private void startAutoScroll() {
    // 先移除之前的回调，防止重复
    autoScrollHandler.removeCallbacks(autoScrollRunnable);

    autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            // 关键修复：添加binding和bannerPager的空检查
            if (binding == null || getContext() == null) {
                return; // 防止内存泄漏
            }

            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.bannerPager.getLayoutManager();
            if (layoutManager == null) return;

            int currentPos = layoutManager.findFirstVisibleItemPosition();
            int nextPos = currentPos + 1;

            // 安全滑动
            binding.bannerPager.smoothScrollToPosition(nextPos);

            // 继续轮播（确保Fragment仍活跃）
            if (isAdded() && !isDetached()) {
                autoScrollHandler.postDelayed(this, 3000);
            }
        }
    };

    // 启动时也检查Fragment状态
    if (isAdded() && !isDetached()) {
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
    }
}






    private void initRefreshLayout() {
        //这些对象一旦设置给 SmartRefreshLayout 后
        // 就由 SmartRefreshLayout 来管理和持有引用 home fragment销毁时 会被释放引用
        // 1. 设置经典刷新头部
        ClassicsHeader header = new ClassicsHeader(requireContext());
        // 新版设置颜色的方法（替换旧版的setPrimaryTextColor/setAccentTextColor）
        header.setLastUpdateText("春风十里扬州站，卷起珠帘总不如"); // 释放时显示的文字
        header.setTextSizeTitle(14f); // 设置标题文字大小（单位：sp）
        header.setAccentColor(Color.GRAY);   // 设置强调文字颜色（如"释放刷新"）
        binding.refreshLayout.setRefreshHeader(header);

        // 2. 新增上拉加载Footer
        ClassicsFooter footer = new ClassicsFooter(requireContext());
        footer.setAccentColor(Color.GRAY); // 与Header风格一致
        binding.refreshLayout.setRefreshFooter(footer);


        //上拉加载
        binding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            viewModel.loadMoreArticles(); // 新增上拉加载
        });

        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            viewModel.refreshAll(); // 替换原来的分步调用
        });

        // 3. 设置下拉高度/回弹效果
        binding.refreshLayout.setHeaderHeight(50);
        binding.refreshLayout.setHeaderTriggerRate(0.6f); // 触发刷新的高度比例
    }

    private void loadInitialData() {
        // 每次加载前清空旧错误
        viewModel.clearErrorMessage(); // 清空之前的错误信息
        viewModel.loadInitialArticles();
        viewModel.loadBanners();
    }




    @Override
    public void onDestroyView() {
        autoScrollHandler.removeCallbacksAndMessages(null);
        binding = null;
        super.onDestroyView();

    }
}