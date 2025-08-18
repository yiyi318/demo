package com.example.myapplication.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.R;
import com.example.myapplication.ui.auth.LoginFragment;
import com.example.myapplication.ui.auth.RegisterFragment;

public class AuthDialogHelper {

    public static void showLoginDialog(
            Context context,
            FragmentManager fragmentManager,
            Runnable onLoginSuccess
    ) {
        if (context == null) return;

        new AlertDialog.Builder(context)
                .setTitle("需要登录")
                .setMessage("请选择登录或注册")
                .setPositiveButton("登录", (dialog, which) ->
                        goToLoginFragment(fragmentManager, onLoginSuccess))
                .setNegativeButton("注册", (dialog, which) ->
                        goToRegisterFragment(fragmentManager))
                .show();
    }

    private static void goToLoginFragment(
            FragmentManager fragmentManager,
            Runnable onLoginSuccess
    ) {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setLoginSuccessListener(() -> {
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        });

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loginFragment)
                .addToBackStack("login")
                .commit();
    }

    private static void goToRegisterFragment(FragmentManager fragmentManager) {
        RegisterFragment registerFragment = new RegisterFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, registerFragment)
                .addToBackStack("register")
                .commit();
    }
}

