package com.example.myapplication.utils.drawer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ProfileContentBinding;
import com.example.myapplication.ui.auth.AuthManager;
import com.example.myapplication.utils.ProfileEventHandler;
import com.example.myapplication.viewmodel.SharedViewModel;

public class NavigationDrawerFragment extends Fragment {
//    private FragmentProfileBinding binding;
    private ProfileContentBinding binding;
    private ProfileEventHandler handler;
    private SharedViewModel sharedViewModel;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = ProfileContentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // 初始化事件处理器
        handler = new ProfileEventHandler(
                requireContext(),
                requireActivity().getSupportFragmentManager(),
                this::updateUI
        );

        sharedViewModel.userLoggedIn.observe(getViewLifecycleOwner(), isLoggedIn -> {
            updateUI(); // 更新侧边栏UI
        });
        setupClickListeners();
    }



    private void setupClickListeners() {
        // 头像点击（严格遵循原始需求）

        binding.ivAvatar.setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_AVATAR_CLICK);
            }

        });
        // 积分排名点击
        binding.ivRank.setOnClickListener(v ->
                {if (handler != null) {
                    handler.handleEvent(ProfileEventHandler.EVENT_RANK_CLICK);
                }}
        );

        // 我的积分点击
        binding.itemPoints.getRoot().setOnClickListener(v -> {
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_POINTS_CLICK);
            }}
        );

        // 我的收藏点击
        binding.itemFavorites.getRoot().setOnClickListener(v ->
                {if (handler != null) {
                    handler.handleEvent(ProfileEventHandler.EVENT_FAVORITES_CLICK);
                }}
        );

        // 分享点击
        binding.itemShare.getRoot().setOnClickListener(v ->
                { if (handler != null) {
                    handler.handleEvent(ProfileEventHandler.EVENT_SHARE_CLICK);
                }}

        );

        // 浏览历史点击
        binding.itemHistory.getRoot().setOnClickListener(v ->
                {if (handler != null) {
                    handler.handleEvent(ProfileEventHandler.EVENT_HISTORY_CLICK);
                }}
        );

        // 设置点击
        binding.itemSettings.getRoot().setOnClickListener(v ->
                {if (handler != null) {
                    handler.handleEvent(ProfileEventHandler.EVENT_SETTINGS_CLICK);
                }}
        );

        // 退出登录点击
        binding.itemBack.getRoot().setOnClickListener(v ->{
            if (handler != null) {
                handler.handleEvent(ProfileEventHandler.EVENT_LOGOUT_CLICK);
            }
        }
        );
    }


    //打开侧边栏的时候，刷新数据
    public void refreshData() {
        updateUI();
    }

    //更新UI
    public void updateUI() {
        if (binding == null) return;

        boolean isLoggedIn = AuthManager.isLoggedIn(requireContext());
        binding.itemBack.getRoot().setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);

        if (isLoggedIn) {
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            Log.d("profile_drawer", "binding_ isLoggedin的状态是" +isLoggedIn);

            binding.tvUsername.setText(prefs.getString("username", "已登录用户"));
            binding.ivAvatar.setImageResource(R.drawable.ic_profile_0);
            binding.rank.setText(prefs.getString("rank", "--"));
            binding.level.setText(prefs.getString("level", "--"));
            binding.itemPoints.tvCoin.setText(prefs.getString("coin_count","--"));
        } else {
            sharedViewModel.setLoggedIn(false);
            binding.tvUsername.setText("点击头像登录");
            binding.ivAvatar.setImageResource(R.drawable.ic_profile_1);
            binding.rank.setText("--");
            binding.level.setText("--");
            binding.itemPoints.tvCoin.setText("--");

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // 确保每次进入页面时数据最新
        updateUI();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
