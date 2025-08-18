package com.example.myapplication.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.adapter.HistoryAdapter;
import com.example.myapplication.model.History;
import com.example.myapplication.viewmodel.profile.HistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * 历史记录页面 Fragment，用于展示用户浏览历史记录。
 * 支持查看历史文章、删除单条记录和清空所有记录功能。
 */
public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private HistoryAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView rvHistory;

    /**
     * 创建 HistoryFragment 实例
     *
     * @return 配置好的 HistoryFragment 实例
     */
    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initViewModel();
    }

    /**
     * 初始化视图组件
     *
     * @param view Fragment 根视图
     */
    private void initViews(View view) {
        setupToolbar(view);
        setupRecyclerView(view);
        setupAdapter();
        setupClickListeners(view);
    }

    /**
     * 设置工具栏
     *
     * @param view Fragment 根视图
     */
    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.historytoolbar);
        if (toolbar == null) return;

        TextView tvTitle = toolbar.findViewById(R.id.toolbar_title);
        if (tvTitle != null) {
            tvTitle.setText(R.string.history_record);
        }

        ImageButton tvBack = toolbar.findViewById(R.id.qr_code_button);
        if (tvBack != null) {
            tvBack.setImageResource(R.drawable.ic_arrow_left);
            tvBack.setOnClickListener(v -> handleBackClick());
        }
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
     *
     * @param view Fragment 根视图
     */
    private void setupRecyclerView(View view) {
        rvHistory = view.findViewById(R.id.rv_history);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }
    /**
     * 设置适配器
     */
    private void setupAdapter() {
        adapter = new HistoryAdapter();
        if (rvHistory != null) {
            rvHistory.setAdapter(adapter);
        }
        adapter.setOnItemClickListener(createHistoryItemClickListener());
    }

    /**
     * 创建历史记录项点击监听器
     *
     * @return HistoryAdapter.OnItemClickListener 实例
     */
    private HistoryAdapter.OnItemClickListener createHistoryItemClickListener() {
        return new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onClick(History history) {
                handleHistoryItemClick(history);
            }

            @Override
            public void onLongClick(History history) {
                showDeleteHistoryDialog(history);
            }
        };
    }

    /**
     * 处理历史记录项点击事件
     *
     * @param history 被点击的历史记录
     */
    private void handleHistoryItemClick(History history) {
        if (history == null || getContext() == null) return;
        Intent intent = new Intent(getContext(), WebViewActivity.class);
        intent.putExtra("url", history.getLink());
        intent.putExtra("title", history.getTitle());
        startActivity(intent);
    }

    /**
     * 显示删除历史记录确认对话框
     *
     * @param history 要删除的历史记录
     */
    private void showDeleteHistoryDialog(History history) {
        if (getContext() == null || history == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_article)
                .setMessage(R.string.confirm_delete_article)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteHistory(history))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * 删除指定的历史记录
     *
     * @param history 要删除的历史记录
     */
    private void deleteHistory(History history) {
        if (historyViewModel != null && history != null) {
            historyViewModel.deleteHistory(history);
        }
    }

    /**
     * 设置点击监听器
     *
     * @param view Fragment 根视图
     */
    private void setupClickListeners(View view) {
        tvEmpty = view.findViewById(R.id.tv_empty);
        setupClearButtonClickListener(view);
    }

    /**
     * 设置清空按钮点击监听器
     *
     * @param view Fragment 根视图
     */
    private void setupClearButtonClickListener(View view) {
        Button btnClear = view.findViewById(R.id.btn_clear);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> showClearAllHistoriesDialog());
        }
    }

    /**
     * 显示清空所有历史记录确认对话框
     */
    private void showClearAllHistoriesDialog() {
        if (getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_articles)
                .setMessage(R.string.confirm_clear_all_articles)
                .setPositiveButton(R.string.delete, (dialog, which) -> clearAllHistories())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * 清空所有历史记录
     */
    private void clearAllHistories() {
        if (historyViewModel != null) {
            historyViewModel.clearAllHistories();
        }
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        observeHistoryData();
    }

    /**
     * 观察历史记录数据变化
     */
    private void observeHistoryData() {
        if (historyViewModel == null) return;
        historyViewModel.getAllHistories().observe(getViewLifecycleOwner(), this::updateHistoryList);
    }

    /**
     * 更新历史记录列表
     *
     * @param histories 历史记录列表
     */
    private void updateHistoryList(List<History> histories) {
        if (histories != null && !histories.isEmpty()) {
            showHistoryList(histories);
        } else {
            showEmptyState();
        }
    }

    /**
     * 显示历史记录列表
     *
     * @param histories 历史记录列表
     */
    private void showHistoryList(List<History> histories) {
        if (adapter != null) {
            adapter.setData(histories);
        }

        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }

        if (rvHistory != null) {
            rvHistory.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示空状态
     */
    private void showEmptyState() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.VISIBLE);
        }

        if (rvHistory != null) {
            rvHistory.setVisibility(View.GONE);
        }
    }
}
