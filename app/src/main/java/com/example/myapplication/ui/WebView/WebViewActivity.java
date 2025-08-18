package com.example.myapplication.ui.WebView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.example.myapplication.R;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.History;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.viewmodel.profile.HistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * WebViewActivity 是一个用于展示网页内容的 Activity。
 * 它使用 Android WebView 组件加载指定 URL，并提供收藏、分享、返回等功能。
 * 同时支持记录浏览历史、注入浅色主题样式、处理页面导航等。
 */
public class WebViewActivity extends AppCompatActivity {
    /**
     * JavaScript代码常量，用于获取网页标题
     */
    private static final String JS_GET_TITLE = "document.title";

    /**
     * 默认分享标题常量
     */
    private static final String DEFAULT_SHARE_TITLE = "分享这篇文章";

    /**
     * WebView组件，用于显示网页内容
     */
    private WebView webView;

    /**
     * TextView组件，用于显示文本内容
     */
    private TextView textView;

    /**
     * ImageButton组件，用于显示图片按钮
     */
    private ImageButton imageButton;

    /**
     * 保存按钮，用于保存操作
     */
    private ImageButton btnSave;

    /**
     * 进度条，用于显示加载进度
     */
    private ProgressBar progressBar;

    /**
     * 顶部工具栏，使用Material设计风格
     */
    private MaterialToolbar webtoolbar; // 使用正确类型

    /**
     * 历史记录ViewModel，用于管理历史记录数据
     */
    private HistoryViewModel historyViewModel; // 提前声明 ViewModel

    /**
     * 分享按钮图标
     */
    private ImageButton icShare;

    /**
     * 数据仓库，用于数据访问和管理
     */
    private Repository repository;

    /**
     * 文章ID，用于标识当前文章
     */
    private int articleId;

    /**
     * 收藏状态标识，true表示已收藏，false表示未收藏
     */
    private boolean isCollected; // 当前收藏状态

    /**
     * 位置ID，用于记录当前位置信息
     */
    private int positionId;

    /**
     * 原始ID，用于标识原始数据
     */
    private int originId;

    /**
     * 网页URL地址
     */
    private String url;

    /**
     * 网页标题
     */
    private String title;

    /**
     * 历史记录章节ID
     */
    private int history_chapterId;

    /**
     * 历史记录链接地址
     */
    private String history_link;

    /**
     * 历史记录标题
     */
    private String history_title;


    @SuppressLint({"MissingInflatedId", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        // 初始化数据相关组件和变量
        initData();

        // 初始化ViewModel，用于数据绑定和业务逻辑处理
        initViewModel();

        // 初始化界面控件和UI相关组件
        initView();

        // 初始化配置参数和相关设置
        initConfiguration();

    }

    /**
     * 初始化 ViewModel，包括 Repository 和 HistoryViewModel。
     * 此方法在 onCreate 中调用，用于准备数据访问层和 ViewModel。
     */
    private void initViewModel() {
        repository = new Repository(RetrofitClient.getService());
        //初始化 ViewModel（在 onCreate 中）
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }


    /**
     * 初始化从 Intent 传递的数据。
     * 包括文章链接、标题、是否已收藏状态、文章 ID 等信息。
     */
    private void initData() {
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        isCollected=getIntent().getBooleanExtra("iscollect",false);
        articleId=getIntent().getIntExtra("articleId",0);
        positionId=getIntent().getIntExtra("positionId",0);
        originId=getIntent().getIntExtra("originId",0);
        history_link = getIntent().getStringExtra("url");
        history_title= getIntent().getStringExtra("title");
        history_chapterId = getIntent().getIntExtra("chapter_id", 0);
    }

    /**
     * 初始化界面组件。
     * 包括工具栏中的按钮、WebView、进度条等 UI 元素，并设置点击事件。
     */
    private void initView() {
        webtoolbar = findViewById(R.id.webviewtoolbar);
        if (webtoolbar != null) {
            btnSave = webtoolbar.findViewById(R.id.ic_share);
            textView = webtoolbar.findViewById(R.id.toolbar_title);
            imageButton = webtoolbar.findViewById(R.id.qr_code_button);
            icShare = webtoolbar.findViewById(R.id.btnSave);

        }
        //进度条
        progressBar = findViewById(R.id.progressBar);

        webView = findViewById(R.id.webView);

        // 禁止 WebView 内部主题适配
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
        }

        // 点击收藏按钮
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> toggleCollect());
            updateCollectButtonUI();
        }



        if (progressBar != null) {
            progressBar.setVisibility(View.GONE); // 默认隐藏
        }


        if (textView != null) {
            textView.setSelected(true); // 关键！激活跑马灯效果
            textView.setText(title);
        }

        if (icShare != null) {
            icShare.setImageResource(R.drawable.ic_share);
            icShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareWebPage();
                }
            });
        }

        if (imageButton != null) {
            imageButton.setImageResource(R.drawable.ic_arrow_left);
            imageButton.setOnClickListener(v -> onBackPressed());
        }
    }

    /**
     * 配置 WebView 的基本设置。
     * 包括启用 JavaScript、DOM 存储，安全配置，以及设置 WebChromeClient 和 WebViewClient。
     * 同时加载传入的 URL。
     */
    private void initConfiguration() {
    if (webView == null) return;

    WebSettings settings = webView.getSettings();

    // 启用必要功能
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);

    // 安全设置
    settings.setAllowFileAccess(false);
    settings.setAllowContentAccess(false);
    settings.setAllowFileAccessFromFileURLs(false);
    settings.setAllowUniversalAccessFromFileURLs(false);

    // 设置客户端回调
    webView.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (progressBar != null) {
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                progressBar.setProgress(newProgress);
            }
        }
    });

    webView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            History history = new History(history_chapterId, history_title, history_link, System.currentTimeMillis());
            historyViewModel.addOrUpdateHistory(history);
        }
    });

    if (url != null && !url.isEmpty()) {
        webView.loadUrl(url);
    }
}



    /**
     * 分享当前网页内容。
     * 获取网页标题和 URL，通过系统分享弹窗进行分享。
     */
    private void shareWebPage() {
        if (webView == null) return;

        // 获取当前网页URL
        final String currentUrl = webView.getUrl();
        if (currentUrl == null || currentUrl.isEmpty()) {
            Toast.makeText(this, "网页未加载完成", Toast.LENGTH_SHORT).show();
            return;
        }

        // 异步获取网页标题
        webView.evaluateJavascript(JS_GET_TITLE, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                // 处理获取到的标题（移除JSON转义引号）
                String shareTitle = value != null ?
                        value.replaceAll("^\"|\"$", "") : DEFAULT_SHARE_TITLE;

                // 创建分享Intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + currentUrl);

                // 启动系统分享弹窗
                startActivity(Intent.createChooser(shareIntent, "分享到"));
            }
        });
    }

    /**
     * 切换收藏状态。
     * 如果用户未登录则提示登录；否则更新 UI 并发送网络请求。
     */
    private void toggleCollect() {
        if (!AuthManager.isLoggedIn(this)) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 乐观更新
        boolean newState = !isCollected;
        isCollected = newState;
        //先更新UI
        updateCollectButtonUI();

        // 设置返回数据（关键！）
        Intent resultData = new Intent();
        resultData.putExtra("positionId", positionId);
        resultData.putExtra("is_collected", newState);
        resultData.putExtra("articleId",articleId);
        resultData.putExtra("originId",originId);

        setResult(RESULT_OK, resultData); // 标记需要更新列表
        Log.d("collectapp", "toggleCollect: "+positionId);

        // 发起网络请求 如果为true 执行收藏 如果为false 执行取消收藏
        Call<BaseResponse> call = isCollected ?
                repository.collectArticle(articleId) :
                repository.uncollectArticle(articleId);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    // 失败时回滚返回数据
                    setResult(RESULT_CANCELED);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                setResult(RESULT_CANCELED);
            }
        });
    }

    /**
     * 更新收藏按钮的图标和内容描述。
     * 根据当前收藏状态显示不同的图标和提示文本。
     */
    private void updateCollectButtonUI() {
        if (btnSave != null) {
            btnSave.setImageResource(
                    isCollected ? R.drawable.ic_collect_filled: R.drawable.ic_collect_outline
            );
            btnSave.setContentDescription(
                    isCollected ? "已收藏" : "收藏"
            );
        }
    }

    /**
     * 处理返回键按下事件。
     * 如果 WebView 可以返回，则执行 goBack；否则调用父类的 onBackPressed 方法。
     */
    @Override
    public void onBackPressed() {
        if (webView != null) {
            WebBackForwardList history = webView.copyBackForwardList();
            int currentIndex = history.getCurrentIndex();

            if (webView.canGoBack() && currentIndex > 0) {
                webView.goBack();
            } else {
                super.onBackPressed(); // 直接关闭
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 在 Activity 销毁时释放 WebView 资源。
     * 防止内存泄漏。
     */
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
