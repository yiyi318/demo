package com.example.myapplication.ui.system.tutorial;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.adapter.TutorialArticleAdapter;
import com.example.myapplication.databinding.FragmentTutorialBinding;
import com.example.myapplication.model.TutorialArticle;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.viewmodel.system.TutorialArticleViewModel;

/**
 * 教程页面的Fragment，用于展示教程文章列表，并支持点击跳转到详情页。
 */
public class TutorialFragment extends Fragment {
    private FragmentTutorialBinding binding;
    private TutorialArticleViewModel tutorialVM;
    private TutorialArticleAdapter tutorialAdapter;




    /**
     * 创建Fragment的视图。
     *
     * @param inflater 用于加载布局的LayoutInflater
     * @param container 父容器ViewGroup
     * @param savedInstanceState 保存的状态数据
     * @return 返回Fragment的根视图
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTutorialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后进行初始化操作。
     *
     * @param view 当前Fragment的根视图
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initViewModel();
        observeData();
        initloaddata();

    }

    private void initViewModel() {
        SystemRepository repository = new SystemRepository(RetrofitClient.getService());
        tutorialVM = new ViewModelProvider(this,
                new SystemViewModelFactory(repository)).get(TutorialArticleViewModel.class);
    }

    private void initloaddata() {
        tutorialVM.refreshTutorialArticles();
    }

    /**
     * 初始化RecyclerView及其相关配置。
     */
    private void initRecyclerView() {
        tutorialAdapter = new TutorialArticleAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(tutorialAdapter);
        binding.recyclerView.setHasFixedSize(true); // 优化性能

        tutorialAdapter.setOnItemClickListener(new TutorialArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TutorialArticle article) {
                handleTutorialArticleClick(article);
            }
        });
    }

    /**
     * 监听ViewModel中的教程文章数据变化并更新UI。
     */
    private void observeData() {
        tutorialVM.getTutorialArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                tutorialAdapter.setArticles(articles);
                Log.d("Tutorial", "显示数据量: " + articles.size());
            } else {
                Log.w("Tutorial", "数据为空");
            }
        });
    }

    /**
     * 处理教程文章项的点击事件，跳转到详情页。
     *
     * @param article 被点击的文章对象
     */
    private void handleTutorialArticleClick(TutorialArticle article) {

        int courseId = article.getId();
        String tutorialName = article.getName();
        String author = article.getAuthor();
        String coverUrl = article.getCover();
        String license = article.getLisense(); // 注意拼写修正
        String desc = article.getDesc();

        TutorialSecondFragment fragment = TutorialSecondFragment.newInstance(
                courseId,
                tutorialName,
                author,
                coverUrl,
                license,
                desc
        );

        try {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_content, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e("TutorialFragment", "Fragment跳转失败", e);
        }
    }

    /**
     * 销毁视图时释放资源。
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 自定义ViewModel工厂类，用于创建带参数的ViewModel实例。
     */
    private static class SystemViewModelFactory implements ViewModelProvider.Factory {
        private final SystemRepository repository;

        public SystemViewModelFactory(SystemRepository repository) {
            this.repository = repository;
        }

        /**
         * 创建ViewModel实例。
         *
         * @param modelClass ViewModel的类型
         * @param <T> ViewModel的泛型类型
         * @return 返回创建的ViewModel实例
         * @throws IllegalArgumentException 如果传入的ViewModel类型不支持
         */
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TutorialArticleViewModel.class)) {
                return modelClass.cast(new TutorialArticleViewModel(repository));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
