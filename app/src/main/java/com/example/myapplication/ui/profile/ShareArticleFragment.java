package com.example.myapplication.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentShareArticleBinding;
import com.example.myapplication.viewmodel.profile.ShareViewModel;

/**
 * 分享文章页面 Fragment，用于用户分享自定义文章链接。
 * 包含标题输入、链接输入和分享按钮，支持实时验证和提交功能。
 */
public class ShareArticleFragment extends Fragment {
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int SUCCESS_DELAY_MILLIS = 2000;
    private static final int COLOR_GRAY = Color.GRAY;
    private static final int COLOR_GREEN = Color.GREEN;

    private FragmentShareArticleBinding binding;
    private ShareViewModel viewModel;

    /**
     * 创建 ShareArticleFragment 实例
     *
     * @return 配置好的 ShareArticleFragment 实例
     */
    public static ShareArticleFragment newInstance() {
        return new ShareArticleFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShareArticleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
               // 初始化ViewModel，建立数据绑定和业务逻辑处理
        initViewModel();

        // 设置工具栏，配置标题和导航功能
        setupToolbar();

        // 初始化UI状态，设置初始显示状态和默认值
        initUIState();

        // 设置输入监听器，处理用户输入事件
        setupInputListeners();

        // 设置点击监听器，处理按钮和视图点击事件
        setupClickListeners();

    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ShareViewModel.class);
    }

    /**
     * 设置工具栏
     */
    private void setupToolbar() {
        if (binding == null) return;

        binding.sharetoolbar.toolbarTitle.setText(R.string.share_article);
        binding.sharetoolbar.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        binding.sharetoolbar.qrCodeButton.setOnClickListener(v -> handleBackClick());
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
     * 初始化 UI 状态
     */
    private void initUIState() {
        if (binding == null) return;

        binding.btnShare.setEnabled(false);
        binding.btnShare.setBackgroundColor(COLOR_GRAY);
    }

    /**
     * 设置输入框监听器
     */
    private void setupInputListeners() {
        if (binding == null) return;

        TextWatcher textWatcher = createInputTextWatcher();
        binding.etTitle.addTextChangedListener(textWatcher);
        binding.etLink.addTextChangedListener(textWatcher);
    }

    /**
     * 创建输入框文本监听器
     *
     * @return TextWatcher 实例
     */
    private TextWatcher createInputTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 输入前不需要特殊处理
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入过程中不需要特殊处理
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        if (binding == null) return;

        binding.btnShare.setOnClickListener(v -> submitArticle());
    }

    /**
     * 验证输入是否合法
     */
    private void validateInputs() {
        if (binding == null) return;

        String title = binding.etTitle.getText().toString().trim();
        String link = binding.etLink.getText().toString().trim();

        boolean isValid = isInputValid(title, link);

        updateShareButtonState(isValid);
    }

    /**
     * 检查输入是否有效
     *
     * @param title 文章标题
     * @param link  文章链接
     * @return true表示输入有效，false表示无效
     */
    private boolean isInputValid(String title, String link) {
        return !title.isEmpty() &&
                title.length() <= TITLE_MAX_LENGTH &&
                URLUtil.isValidUrl(link);
    }

    /**
     * 更新分享按钮状态
     *
     * @param isEnabled 按钮是否启用
     */
    private void updateShareButtonState(boolean isEnabled) {
        if (binding == null) return;

        binding.btnShare.setEnabled(isEnabled);
        int color = isEnabled ?
                ContextCompat.getColor(requireContext(), R.color.blue) :
                COLOR_GRAY;
        binding.btnShare.setBackgroundColor(color);
    }

    /**
     * 提交文章到服务器
     */
    private void submitArticle() {
        if (binding == null) return;

        String title = binding.etTitle.getText().toString();
        String link = binding.etLink.getText().toString();

        showProgress(true);
        viewModel.postShareArticle(title, link).observe(getViewLifecycleOwner(), this::handleSubmissionResult);
    }

    /**
     * 显示或隐藏进度条
     *
     * @param show 是否显示进度条
     */
    private void showProgress(boolean show) {
        if (binding == null) return;

        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);

        binding.btnShare.setEnabled(!show);
        if (show) {
            binding.btnShare.setBackgroundColor(Color.BLUE);
        }
    }

    /**
     * 处理提交结果
     *
     * @param success 提交是否成功
     */
    private void handleSubmissionResult(Boolean success) {
        if (success != null && success) {
            handleSuccess();
        } else {
            handleFailure();
        }
    }

    /**
     * 处理提交成功的情况
     */
    private void handleSuccess() {
        if (binding == null) return;

        binding.btnShare.setText("✓");
        binding.btnShare.setBackgroundColor(COLOR_GREEN);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && getActivity() != null) {
                try {
                    requireActivity().getSupportFragmentManager().popBackStack();
                } catch (Exception e) {
                    // 忽略异常，避免崩溃
                }
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    /**
     * 处理提交失败的情况
     */
    private void handleFailure() {
        showProgress(false);
        updateShareButtonState(true);

        if (getContext() != null) {
            Toast.makeText(getContext(), "分享失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
