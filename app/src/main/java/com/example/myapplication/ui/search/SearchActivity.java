package com.example.myapplication.ui.search;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.model.HotKey;
import com.example.myapplication.utils.Broadcast.NetworkChangeReceiver;
import com.example.myapplication.viewmodel.search.HotKeyViewModel;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

/**
 * 搜索页面 Activity，用于处理用户搜索请求和展示热门搜索关键词。
 * 包含搜索输入框、返回按钮、搜索按钮和热门关键词标签云。
 */
public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final int TAG_PADDING_LEFT = 32;
    private static final int TAG_PADDING_TOP = 16;
    private static final int TAG_PADDING_RIGHT = 32;
    private static final int TAG_PADDING_BOTTOM = 16;
    private static final int TAG_MARGIN_RIGHT = 16;
    private static final int TAG_MARGIN_BOTTOM = 16;

    private FlexboxLayout flexboxLayout;
    private HotKeyViewModel viewModel;
    private EditText searchInput;
    private NetworkChangeReceiver receiver;
    private ImageButton btnBack;
    private ImageButton btnSearch;
    /**
     * Activity 创建时调用，初始化界面和相关组件
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置当前 Activity 的布局文件为搜索页面布局
        setContentView(R.layout.activity_search);

        // 初始化网络状态广播接收器，用于监听网络连接状态变化
        initNetworkReceiver();

        // 初始化界面中的各个视图组件
        initViews();

        // 设置界面中各控件的点击事件监听器
        setupClickListeners();

        // 设置搜索输入框的输入监听器，处理用户输入搜索关键词的逻辑
        setupSearchInputListener();

        // 初始化 ViewModel，用于管理搜索相关的数据和业务逻辑
        initViewModel();

    }

    /**
     * 初始化网络状态监听器
     */
    private void initNetworkReceiver() {
        receiver = new NetworkChangeReceiver();
        try {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, filter);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register network receiver", e);
        }
    }

    /**
     * 初始化视图组件
     */
    @SuppressLint("WrongViewCast")
    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        flexboxLayout = findViewById(R.id.flexbox_hot_keys);
        btnBack = findViewById(R.id.btn_back);
        btnSearch = findViewById(R.id.btn_search);

        // 空值检查
        if (searchInput == null || flexboxLayout == null || btnBack == null || btnSearch == null) {
            Log.e(TAG, "Some views not found in layout");
        }
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> handleSearchClick());
        }
    }

    /**
     * 处理搜索按钮点击事件
     */
    private void handleSearchClick() {
        String query = searchInput != null ? searchInput.getText().toString().trim() : "";
        if (!query.isEmpty()) {
            performSearch(query);
        } else {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置输入框键盘搜索键监听
     */
    private void setupSearchInputListener() {
        if (searchInput != null) {
            searchInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(v.getText().toString());
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(HotKeyViewModel.class);
        viewModel.getHotKeys().observe(this, this::updateHotKeyTags);
        viewModel.loadHotKeys();
    }

    /**
     * 执行搜索操作
     *
     * @param query 搜索关键词
     */
    private void performSearch(String query) {
        showSearchResults(query);
    }

    /**
     * 显示搜索结果页面
     *
     * @param query 搜索关键词
     */
    private void showSearchResults(String query) {
        findViewById(R.id.nestedScrollView).setVisibility(View.GONE);

        SearchResultFragment fragment = SearchResultFragment.newInstance(query);
        try {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show search results", e);
            Toast.makeText(this, "搜索失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新热门关键词标签
     *
     * @param hotKeys 热门关键词列表
     */
    private void updateHotKeyTags(List<HotKey> hotKeys) {
        if (flexboxLayout == null || hotKeys == null) return;

        flexboxLayout.removeAllViews();
        for (HotKey hotKey : hotKeys) {
            TextView tagView = createHotKeyTag(hotKey);
            FlexboxLayout.LayoutParams params = createTagLayoutParams();
            flexboxLayout.addView(tagView, params);
        }
    }

    /**
     * 创建热门关键词标签视图
     *
     * @param hotKey 热门关键词
     * @return 标签 TextView
     */
    private TextView createHotKeyTag(HotKey hotKey) {
        TextView tagView = new TextView(this);
        tagView.setText(hotKey.getName());

        // 设置标签样式
        tagView.setPadding(TAG_PADDING_LEFT, TAG_PADDING_TOP, TAG_PADDING_RIGHT, TAG_PADDING_BOTTOM);
        tagView.setBackgroundResource(R.drawable.tag_bg);

        tagView.setOnClickListener(v -> {
            if (searchInput != null) {
                searchInput.setText(hotKey.getName());
                searchInput.setSelection(hotKey.getName().length());
            }
        });

        return tagView;
    }

    /**
     * 创建标签布局参数
     *
     * @return FlexboxLayout.LayoutParams
     */
    private FlexboxLayout.LayoutParams createTagLayoutParams() {
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, TAG_MARGIN_RIGHT, TAG_MARGIN_BOTTOM);
        return params;
    }

    /**
     * 注销网络状态监听器
     */
    private void unregisterNetworkReceiver() {
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
                receiver = null;
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister network receiver", e);
            }
        }
    }

    /**
     * Activity 销毁时调用，释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkReceiver();
    }



}
