package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentLoginBinding;
import com.example.myapplication.viewmodel.Auth.AuthViewModel;
import com.google.gson.JsonObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link com.example.myapplication.ui.auth.LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/**
 * 登录页面 Fragment
 * 提供用户登录功能，包括用户名密码验证、用户信息获取和UI更新
 * 支持登录成功回调和登出监听
 */
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private Runnable loginSuccessListener;

    /**
     * 设置登录成功回调监听器
     *
     * @param listener 登录成功后的回调函数
     */
    public void setLoginSuccessListener(Runnable listener) {
        this.loginSuccessListener = listener;
    }


    /**
     * 创建 Fragment 视图
     *
     * @param inflater           LayoutInflater
     * @param container          ViewGroup 容器
     * @param savedInstanceState 保存的状态数据
     * @return Fragment 根视图
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后初始化UI组件和数据
     *
     * @param view               Fragment 根视图
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();

        // 设置工具栏
        setupToolbar();
        // 设置数据观察
        setupObservers();
        // 设置点击事件监听器
        setupClickListeners();
    }

    private void initViewModel() {
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    /**
     * 设置工具栏标题和返回按钮
     */
    private void setupToolbar() {
        binding.logintoolbar.toolbarTitle.setText("登录页面");
        binding.logintoolbar.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        binding.logintoolbar.qrCodeButton.setOnClickListener(v->
                requireActivity().getSupportFragmentManager().popBackStack());
    }




        /**
     * 设置观察者，用于监听ViewModel中的数据变化并更新UI
     * 该方法主要观察用户信息、错误信息和登录结果三个LiveData对象
     */
    private void setupObservers() {
        // 在观察前清空之前的登录结果，避免使用旧数据
        viewModel.clearLoginResult();

        // 观察用户信息变化，当获取到用户信息时保存到本地并通知父Fragment更新UI
        viewModel.getUserInfoLiveData().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                try {
                    if (response.has("data") && response.get("data").isJsonObject()) {
                        JsonObject userInfo = response.getAsJsonObject("data");
                        AuthManager.saveUserInfo(requireContext(), userInfo);
                        requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        if (loginSuccessListener != null) {
                            loginSuccessListener.run();
                        }
                    }
                } catch (Exception e) {
                    Log.e("Login", "解析用户信息失败", e);
                }
            }
        });

        // 观察错误信息变化，当有错误信息时显示Toast提示
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && isAdded()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // 观察登录结果变化，根据登录成功或失败执行相应操作
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            // 添加null检查
            if (result != null) {
                if (result.isSuccess()) {
                    AuthManager.saveLoginInfo(requireContext(), result.getData().getUsername(), "cookie");
                    viewModel.fetchUserInfo(); // 获取用户详细信息
                    binding.btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green));
                    binding.btnLogin.setText("登录成功");
                } else {
                    Toast.makeText(requireContext(), result.getErrorMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        initTextWatch();

        // 加载状态监听
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loginProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });
    }

    private void initTextWatch(){
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        binding.etUsername.addTextChangedListener(watcher);
        binding.etPassword.addTextChangedListener(watcher);
    }



    private void updateLoginButtonState() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        boolean canLogin = !username.isEmpty() && !password.isEmpty();
        binding.btnLogin.setEnabled(canLogin);

        if (canLogin) {
            binding.btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
        } else {
            binding.btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_light));
        }
    }




    /**
     * 设置点击事件监听器
     * 包括登录按钮和注册按钮点击事件
     */
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(), "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(username, password);
        });

        binding.register.setOnClickListener(v->{
            replaceFragmentWithBackStack(new RegisterFragment(),"RegisterFragment");
        });
    }


    /**
     * 带回退栈的Fragment替换操作
     *
     * @param fragment 要替换的目标Fragment
     * @param tag      Fragment标签
     */
    private void replaceFragmentWithBackStack(Fragment fragment, String tag) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(tag)
                .commit();
    }

}
