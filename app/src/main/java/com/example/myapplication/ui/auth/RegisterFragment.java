package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentRegisterBinding;
import com.example.myapplication.viewmodel.Auth.AuthViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link com.example.myapplication.ui.auth.RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/**
 * 用户注册页面 Fragment
 * 提供用户注册功能，包括输入验证、注册请求和注册成功后的页面跳转
 */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

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
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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

        // 设置注册按钮点击监听器
        setupRegisterButton();
        //初始化ViewModel
        initViewModel();

        // 设置工具栏
        setupToolbar();
        //初始化数据
        initObserveData();

        // 设置跳转到登录页面的点击监听器
        setupGoToLoginListener();
    }
    /**
     * 初始化ViewModel实例
     * 通过ViewModelProvider获取AuthViewModel的实例并赋值给成员变量viewModel
     */
    void initViewModel() {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    /**
     * 初始化数据观察
     * 设置LiveData的观察者，监听注册相关数据变化并执行相应操作
     */
    private void initObserveData() {
        // 观察注册消息提示无论 getRegisterMessage()返回什么消息（成功、失败、错误提示等），都会通过 showToast方法将其显示为 Toast 提示
        viewModel.getRegisterMessage().observe(getViewLifecycleOwner(), this::showToast);

        // 观察注册成功状态，成功则跳转到登录页面
        viewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                navigateToLogin(); // 注册成功跳转
            }
        });

        initTextWatch();
// 加载状态监听
        viewModel.getIsRegistering().observe(getViewLifecycleOwner(), isRegistering -> {
            binding.registerProgressBar.setVisibility(isRegistering ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isRegistering);
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
        binding.etConfirmPassword.addTextChangedListener(watcher);
    }

    private void updateLoginButtonState() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // 检查所有字段都不为空，且密码一致
        boolean canRegister = !username.isEmpty()
                && !password.isEmpty()
                && !confirmPassword.isEmpty()
                && password.equals(confirmPassword);

        binding.btnRegister.setEnabled(canRegister);

        if (canRegister) {
            binding.btnRegister.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
        } else {
            binding.btnRegister.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_light));
        }
    }





    /**
     * 设置注册按钮点击监听器
     * 处理用户注册逻辑
     */
    private void setupRegisterButton() {
        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            String repassword = binding.etConfirmPassword.getText().toString();

            // 验证输入信息
            if (!validateInput(username, password, repassword)) {
                return;
            }

            // 执行注册操作
            viewModel.register(username, password, repassword);
        });
    }

    /**
     * 设置工具栏标题和返回按钮
     */
    private void setupToolbar() {
        binding.registertoolbar.toolbarTitle.setText("用户注册");
        binding.registertoolbar.qrCodeButton.setImageResource(R.drawable.ic_arrow_left);
        binding.registertoolbar.qrCodeButton.setOnClickListener(V->
                requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * 设置跳转到登录页面的点击监听器
     */
    private void setupGoToLoginListener() {
        binding.tvGoLogin.setOnClickListener(v->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });
    }



    /**
     * 验证用户输入信息
     *
     * @param username        用户名
     * @param password        密码
     * @param confirmPassword 确认密码
     * @return 验证通过返回true，否则返回false
     */
    private boolean validateInput(String username, String password, String confirmPassword) {
        if (username.isEmpty()) {
            showToast("请输入用户名");
            return false;
        }

        if (password.isEmpty()) {
            showToast("请输入密码");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("两次输入的密码不一致");
            return false;
        }

        return true;
    }


    /**
     * 跳转到登录页面
     */
    private void navigateToLogin() {
        binding.btnRegister.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green));
        binding.btnRegister.setText("注册成功");
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .addToBackStack("login")  // ✅ 加入回退栈
                .commit();
    }

    /**
     * 显示Toast提示信息
     *
     * @param message 要显示的信息
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Fragment销毁时的清理操作
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
