package com.example.myapplication.ui.project;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.example.myapplication.Respository.ProjectArticleRepository;
import com.example.myapplication.adapter.ProjectArticleAdapter;
import com.example.myapplication.model.ProjectArticle;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.utils.AuthDialogHelper;
import com.example.myapplication.viewmodel.SharedCollectViewModel;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.example.myapplication.viewmodel.project.ProjectArticleViewModel;

public class ProjectArticleTabFragment extends Fragment {
    private static final String ARG_TAB_ID = "tab_id";
    private ProjectArticleViewModel viewModel;
    private SharedCollectViewModel collectViewModel;
    private ProjectArticleAdapter adapter;

    //这是 Android 提供的一个 下拉刷新容器，
    private SwipeRefreshLayout swipeRefreshLayout;
    //这是 Android 的 强大列表控件，用来显示可滚动的数据集合，配合 Adapter 使用
    private RecyclerView recyclerView;
    //线性布局管理器
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    private SharedViewModel sharedViewModel;

   private  TextView tvRefreshStatus;

    private static final int REQUEST_CODE_ARTICLE_DETAIL = 1001;


        /**
     * 创建ProjectArticleTabFragment实例的工厂方法
     *
     * @param tabId 选项卡ID，用于标识不同的文章标签页
     * @return 返回配置好参数的ProjectArticleTabFragment实例
     */
    public static ProjectArticleTabFragment newInstance(int tabId) {
        ProjectArticleTabFragment fragment = new ProjectArticleTabFragment();
        Bundle args = new Bundle();
        // 将tabId参数存入Bundle中作为fragment的参数
        args.putInt(ARG_TAB_ID, tabId);
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

       /**
     * 创建并返回Fragment的视图层次结构
     *
     * @param inflater 用于将XML布局文件转换为View对象的LayoutInflater
     * @param container 父容器ViewGroup，当前Fragment的视图将被添加到此容器中
     * @param savedInstanceState 保存的Fragment状态数据，用于恢复之前保存的状态
     * @return 返回创建的View对象，作为Fragment的用户界面
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 修改布局文件，添加 SwipeRefreshLayout
        View view = inflater.inflate(R.layout.fragment_project_article_tab, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        int tabId = getArguments() != null ? getArguments().getInt(ARG_TAB_ID, 0) : 0;

        // 初始化RecyclerView
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ProjectArticleAdapter();
        recyclerView.setAdapter(adapter);

        tvRefreshStatus = view.findViewById(R.id.tvRefreshStatus);



        adapter.setOnItemClickListener(new ProjectArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ProjectArticle article, int position) {
                // 处理文章点击（可选）
                Log.d("project", "onItemClick: ");
                handleArticleClick(article,position);
            }

            @Override
            public void onCollectClick(ProjectArticle article, int position) {
                // 核心收藏逻辑
                handleCollectClick(article, position);
            }
        });

        // 设置刷新监听器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            tvRefreshStatus.setVisibility(View.VISIBLE);
            if (viewModel != null) {
                viewModel.refreshArticles();
            }
        });

        // 添加滚动监听器实现分页加载 用户到下面两个item 自动触发加载
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

        // 初始化ViewModel
        initViewModel(tabId);

        // 观察数据变化
        observeData();
    }


    //文章点击
    private void handleArticleClick(ProjectArticle article, int position) {
//        Toast.makeText(getContext(),"开始点击project文章了",Toast.LENGTH_SHORT).show();
        Log.d("project", "handleArticleClick: 开始点击project文章了");
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
        intent.putExtra("articleId",article.getId());
        intent.putExtra("positionId",position);





        startActivityForResult(intent, REQUEST_CODE_ARTICLE_DETAIL);

        // 4. 降级方案：普通跳转

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ARTICLE_DETAIL && resultCode == RESULT_OK) {
            int articleId = data.getIntExtra("positionId", -1);
            boolean isCollected = data.getBooleanExtra("is_collected", false);
            Log.d("collectapp", "onActivityResult: "+articleId);
            // 直接更新对应Item
            adapter.updateCollectState(articleId, isCollected);
        }
    }

    //收藏点击
    private void handleCollectClick(ProjectArticle article, int position) {

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
                collectViewModel.uncollectArticle(article.getId()) :
                collectViewModel.collectArticle(article.getId());

        operationResult.observe(getViewLifecycleOwner(), success -> {
            if (success == null || !success) {
                // 操作失败，回滚UI状态
                adapter.updateCollectState(position, isCollect);
                String errorMsg = isCollect ? "取消收藏失败" : "收藏失败";
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void initViewModel(int tabId) {
        ProjectArticleRepository repository = new ProjectArticleRepository(RetrofitClient.getService());
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ProjectArticleViewModel(repository, tabId);
            }
        }).get(ProjectArticleViewModel.class);
        collectViewModel=new ViewModelProvider(requireActivity()).get(SharedCollectViewModel.class);
    }

    private void observeData() {
        // 观察文章数据
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                adapter.submitList(articles);
            }
        });

        // 观察加载状态 如果isloading变为false 自动停止刷新
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            this.isLoading = isLoading;
            if (isLoading) {
                tvRefreshStatus.setVisibility(View.VISIBLE);
            } else {
                tvRefreshStatus.setVisibility(View.GONE);
            }
            //根据isLoading的值，更新下拉刷新的加载状态
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // 观察错误信息
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                //错误提示
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

    private void loadMoreData() {
        if (viewModel != null && !isLoading && hasMoreData) {
            viewModel.loadMoreArticles();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 确保Fragment可见时刷新数据
        if (viewModel != null && adapter.getItemCount() == 0) {
            viewModel.refreshArticles();
        }
    }
}