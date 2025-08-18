package com.example.myapplication.ui.profile;

import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.adapter.SettingAdapter;
import com.example.myapplication.databinding.FragmentSettingBinding;
import com.example.myapplication.model.SettingItem;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.utils.CacheUtils;
import com.scwang.smart.drawable.paint.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置界面的Fragment，用于展示和处理各种设置项。
 * 包括主题切换、缓存清理、跳转网页等功能。
 */
public class SettingFragment extends Fragment implements SettingAdapter.OnSettingItemClickListener {

    // 定义设置项的ID常量
    private static final int ITEM_MODE = 1;
    private static final int ITEM_STORAGE = 2;
    private static final int ITEM_CODE = 3;
    private static final int ITEM_VERSION_RE = 4;
    private static final int ITEM_INTERNET = 5;
    private static final int ITEM_PROJECT = 6;
    private static final int ITEM_VERSION = 7;
    private static final int ITEM_ME = 8;

    private FragmentSettingBinding binding;
    private SettingAdapter adapter;
    private List<SettingItem> settingItems = new ArrayList<>();

    /**
     * 创建Fragment的视图层次结构。
     *
     * @param inflater           用于加载Fragment布局的LayoutInflater对象
     * @param container          父容器视图组
     * @param savedInstanceState 保存Fragment状态的Bundle对象
     * @return 返回Fragment的根视图
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        initViews();
        return binding.getRoot();
    }

    /**
     * 初始化视图组件，包括工具栏、RecyclerView和设置项数据。
     */
    private void initViews() {
        // 初始化工具栏
        initToolbar();

        // 初始化RecyclerView
        initRecyclerView();

        // 初始化数据
        initSettingItems();
    }

    /**
     * 初始化工具栏，设置标题和返回按钮。
     */
    private void initToolbar() {
        binding.settingtoolbar.toolbarTitle.setText(R.string.setting);
        binding.settingtoolbar.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        binding.settingtoolbar.qrCodeButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

    }

    /**
     * 初始化RecyclerView，设置适配器和布局管理器。
     */
    private void initRecyclerView() {
        adapter = new SettingAdapter(settingItems, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    /**
     * 初始化设置项列表，根据当前UI模式设置主题切换项的显示内容。
     */
    @SuppressLint("NotifyDataSetChanged")
    private void initSettingItems() {
        int currentNightMode = getResources().getConfiguration().uiMode & UI_MODE_NIGHT_MASK;
        String modeText = (currentNightMode == UI_MODE_NIGHT_YES) ?
                getString(R.string.day_mode) : getString(R.string.night_mode);
        int modeIcon = modeText.equals(getString(R.string.day_mode)) ?
                R.drawable.ic_sun : R.drawable.ic_moon;

        settingItems.clear();
        settingItems.add(new SettingItem(ITEM_MODE, modeText, "", modeIcon, SettingItem.SettingItemType.MODE));
        settingItems.add(new SettingItem(ITEM_STORAGE, getString(R.string.clear_cache),
                CacheUtils.getReadableCacheSize(requireContext()),
                R.drawable.ic_arrow_right, SettingItem.SettingItemType.STORAGE));
        settingItems.add(new SettingItem(ITEM_CODE, getString(R.string.scan_qr_code),
                getString(R.string.feature_pending),
                R.drawable.ic_arrow_right, SettingItem.SettingItemType.ACTION));
        settingItems.add(new SettingItem(ITEM_VERSION_RE, getString(R.string.version_update),
                getString(R.string.feature_pending),
                R.drawable.ic_arrow_right, SettingItem.SettingItemType.ACTION));
        settingItems.add(new SettingItem(ITEM_INTERNET, getString(R.string.official_website),
                "https://www.wanandroid.com/",
                R.drawable.ic_arrow_right, SettingItem.SettingItemType.LINK));
        settingItems.add(new SettingItem(ITEM_PROJECT, getString(R.string.project_source),
                getString(R.string.feature_pending),
                R.drawable.ic_arrow_right, SettingItem.SettingItemType.LINK));
        settingItems.add(new SettingItem(ITEM_VERSION, getString(R.string.version_declaration),
                getString(R.string.version_info),
                R.drawable.ic_arrow_right, SettingItem.SettingItemType.DIALOG));
        settingItems.add(new SettingItem(ITEM_ME, getString(R.string.author_attribution),
                getString(R.string.developer_name),
                R.drawable.ic_arrow_right, SettingItem.SettingItemType.DIALOG));

        adapter.notifyDataSetChanged();
    }

    /**
     * 显示清除缓存确认对话框。
     */
    private void showClearCacheDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.clear_cache)
                .setMessage(R.string.confirm_clear_cache)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    CacheUtils.clearAllCache(requireContext());
                    updateCacheSize();
                    Toast.makeText(requireContext(), R.string.cache_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * 打开指定URL的网页页面。
     *
     * @param url   要打开的网页URL
     * @param title 网页标题
     */
    private void openWebPage(String url, String title) {
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity());
        startActivity(intent, options.toBundle());
    }

    /**
     * 显示一个简单的信息对话框。
     *
     * @param title   对话框标题
     * @param message 对话框内容
     */
    private void showDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * 处理设置项点击事件。
     *
     * @param item 被点击的设置项对象
     */
    @Override
    public void onSettingItemClick(SettingItem item) {
        switch (item.getId()) {
            case ITEM_MODE:
                toggleNightMode(item.getTitle());
                break;
            case ITEM_STORAGE:
                showClearCacheDialog();
                break;
            case ITEM_INTERNET:
                openWebPage(item.getDescription(), "官方网站");
                break;
            case ITEM_PROJECT:
                openWebPage(item.getDescription(), "项目源码");
                break;
            case ITEM_VERSION:
                showDialog("版本", BuildConfig.VERSION_NAME);
                break;
            case ITEM_ME:
                showDialog("关于", "开发者: 吃几个菜");
                break;

            case ITEM_VERSION_RE:
                showToast(item.getTitle() + "待实现");
                break;

            case ITEM_CODE:
                showToast(item.getTitle() + "待实现");
                break;
        }
    }

    /**
     * 切换夜间/日间模式，并更新偏好设置。
     *
     * @param currentModeText 当前模式文本（"夜间模式"或"日间模式"）
     */
    private void toggleNightMode(String currentModeText) {
        int currentUiMode = getResources().getConfiguration().uiMode;
        int newNightMode = (currentUiMode & UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;

        AppCompatDelegate.setDefaultNightMode(newNightMode);
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putInt("night_mode", newNightMode)
                .apply();

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putInt("tab", 4)
                .apply();

        showToast(currentModeText + getString(R.string.mode_set));

    }

    /**
     * 更新缓存大小显示为0KB。
     */
    private void updateCacheSize() {
        for (int i = 0; i < settingItems.size(); i++) {
            SettingItem item = settingItems.get(i);
            if (item.getId() == ITEM_STORAGE) {
                item.setDescription("0KB");
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * 显示短时间Toast提示。
     *
     * @param text 要显示的文本内容
     */
    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 在Fragment恢复时更新缓存大小显示。
     */
    @Override
    public void onResume() {
        super.onResume();
        // 更新缓存大小显示
        new Thread(() -> {
            final String size = CacheUtils.getReadableCacheSize(requireContext());
            requireActivity().runOnUiThread(() -> {
                for (int i = 0; i < settingItems.size(); i++) {
                    SettingItem item = settingItems.get(i);
                    if (item.getId() == ITEM_STORAGE) {
                        item.setDescription(size);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            });
        }).start();
    }

    /**
     * 在Fragment视图销毁时释放资源。
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
