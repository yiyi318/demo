package com.example.myapplication.ui.system.tutorial;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.TutorialSecondAdapter;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.viewmodel.system.TutorialSecondViewModel;

import java.util.ArrayList;

/**
 * 教程二级页面 Fragment，用于展示某个教程的章节列表和基本信息。
 * 包含教程封面、作者、描述、许可证等信息，并通过 RecyclerView 展示章节列表。
 */
public class TutorialSecondFragment extends Fragment {
    private static final String ARG_COURSE_ID = "course_id";
    private static final String ARG_TUTORIAL_NAME = "tutorial_name";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_COVER = "cover";
    private static final String ARG_LICENSE = "license";
    private static final String ARG_DESC = "desc";

    private TutorialSecondAdapter adapter;
    private int courseId;
    private String tutorialName;
    private String author;
    private String coverUrl;
    private String license;
    private String desc;

    private TutorialSecondViewModel viewModel;

    /**
     * 创建 TutorialSecondFragment 实例并传递参数
     *
     * @param courseId     教程课程ID
     * @param tutorialName 教程名称
     * @param author       教程作者
     * @param coverUrl     教程封面图片URL
     * @param license      教程许可证信息
     * @param desc         教程描述信息
     * @return 返回配置好的 TutorialSecondFragment 实例
     */
    public static TutorialSecondFragment newInstance(int courseId,
                                                     String tutorialName,
                                                     String author,
                                                     String coverUrl,
                                                     String license,
                                                     String desc) {
        TutorialSecondFragment fragment = new TutorialSecondFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COURSE_ID, courseId);
        args.putString(ARG_TUTORIAL_NAME, tutorialName);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_COVER, coverUrl);
        args.putString(ARG_LICENSE, license);
        args.putString(ARG_DESC, desc);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getInt(ARG_COURSE_ID);
            tutorialName = getArguments().getString(ARG_TUTORIAL_NAME);
            author = getArguments().getString(ARG_AUTHOR);
            coverUrl = getArguments().getString(ARG_COVER);
            license = getArguments().getString(ARG_LICENSE);
            desc = getArguments().getString(ARG_DESC);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        // 观察数据变化
        observeViewModel();
        initLoadDate();


    }

    private void initLoadDate() {
        viewModel.loadTutorialChapters(courseId);
    }

    private void initViewModel() {
        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(TutorialSecondViewModel.class);

    }

    /**
     * 观察 ViewModel 中的数据变化，包括章节列表、错误信息和加载状态
     */
    private void observeViewModel() {
        // 观察章节数据
        viewModel.getChaptersLiveData().observe(getViewLifecycleOwner(), chapters -> {
            if (chapters != null) {
                adapter.updateData(chapters);
            }
        });

        // 观察错误信息
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && getContext() != null) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // 观察加载状态
        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            View view = getView();
            if (view != null) {
                ProgressBar progressBar = view.findViewById(R.id.progress_bar);
                if (progressBar != null) {
                    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    /**
     * 初始化页面中的各个视图组件，包括 Toolbar、基本信息展示区域和 RecyclerView
     *
     * @param view Fragment 的根视图
     */
    private void initViews(View view) {
        // 初始化Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.course_catalog);
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // 设置教程基本信息
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvAuthor = view.findViewById(R.id.tv_author);
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        TextView tvLicense = view.findViewById(R.id.tv_license);
        ImageView ivCover = view.findViewById(R.id.iv_cover);

        if (tvTitle != null) tvTitle.setText(tutorialName);
        if (tvAuthor != null) tvAuthor.setText(author);
        if (tvDesc != null) tvDesc.setText(desc);
        if (tvLicense != null) tvLicense.setText(license);

        // 加载封面图片
        if (ivCover != null) {
            if (!TextUtils.isEmpty(coverUrl)) {
                Glide.with(this)
                        .load(coverUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .into(ivCover);
            } else {
                ivCover.setImageResource(R.drawable.ic_placeholder);
            }
        }

        // 初始化RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rv_chapters);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new TutorialSecondAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);

            // 设置点击监听器（只设置一次）
            adapter.setOnItemClickListener((chapter, position) -> {
                if (getActivity() != null && chapter != null) {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("url", chapter.getLink());
                    intent.putExtra("title", chapter.getTitle());

                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity());
                    startActivity(intent, options.toBundle());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        // 清理适配器引用
        if (adapter != null) {
            adapter.setOnItemClickListener(null);
            adapter = null;
        }
        super.onDestroyView();
    }
}
