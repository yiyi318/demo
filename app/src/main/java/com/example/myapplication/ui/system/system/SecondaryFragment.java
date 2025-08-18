package com.example.myapplication.ui.system.system;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.model.Chapter;
import com.example.myapplication.viewmodel.SharedViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/**
 * 二级页面 Fragment，用于展示体系结构下的子分类列表。
 * 使用 ViewPager2 和 TabLayout 实现横向滑动切换不同子分类的文章列表。
 */
public class SecondaryFragment extends Fragment {

    private static final String ARG_CHILDREN = "children";
    private static final String ARG_NAME = "name";

    private List<Chapter> children = new ArrayList<>();
    private String name;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SharedViewModel sharedViewModel;

    /**
     * 创建 SecondaryFragment 实例并传递参数
     *
     * @param children 子章节列表
     * @param name     父章节名称
     * @return 配置好的 SecondaryFragment 实例
     */
    public static SecondaryFragment newInstance(List<Chapter> children, String name) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CHILDREN, new ArrayList<>(children));
        args.putString(ARG_NAME, name);

        SecondaryFragment fragment = new SecondaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            children = args.getParcelableArrayList(ARG_CHILDREN);
            name = args.getString(ARG_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_secondary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initToolbar(view);
        initViewModel();
        initViewPagerAndTabLayout(view);
    }

    /**
 * 初始化顶部工具栏，设置标题和返回按钮
 *
 * @param view Fragment 根视图
 */
    private void initToolbar(View view) {
        View toolbar = view.findViewById(R.id.secondtoolbar);
        if (toolbar == null) return;

        TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (titleView != null) {
            if (name != null) {
                titleView.setText(name);
            } else {
                titleView.setText("");
            }
        }

        ImageButton backButton = toolbar.findViewById(R.id.qr_code_button);
        if (backButton != null) {
            backButton.setImageResource(R.drawable.ic_arrow_left);
            backButton.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null && !getActivity().isFinishing() && !isDetached()) {
                    sharedViewModel.setShouldShowBottomTab(true);
                    if (getParentFragmentManager().getBackStackEntryCount() > 0) {

                        Log.d("BackStack", "栈深度: " + getParentFragmentManager().getBackStackEntryCount());
                        getParentFragmentManager().popBackStack();
                    }
                }
            });
        }
    }


    /**
     * 初始化 ViewModel 并设置底部导航栏显示状态
     */
    private void initViewModel() {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.setShouldShowBottomTab(false);
    }

    /**
     * 初始化 ViewPager 和 TabLayout
     *
     * @param view Fragment 根视图
     */
    private void initViewPagerAndTabLayout(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        if (children == null || children.isEmpty()) {
            return;
        }

        setupViewPager();
        setupTabLayout();
        viewPager.setOffscreenPageLimit(1);
    }

    /**
     * 设置 ViewPager 适配器
     */
    private void setupViewPager() {
        viewPager.setAdapter(new ChapterFragmentStateAdapter(this, children));
    }

    /**
     * 设置 TabLayout 与 ViewPager 的联动
     */
    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position >= 0 && position < children.size()) {
                tab.setText(children.get(position).getName());
            }
        }).attach();
    }

    /**
     * 自定义 FragmentStateAdapter，用于管理章节页面
     */
    private static class ChapterFragmentStateAdapter extends FragmentStateAdapter {
        private final List<Chapter> chapters;

        public ChapterFragmentStateAdapter(@NonNull Fragment fragment, List<Chapter> chapters) {
            super(fragment);
            this.chapters = chapters != null ? chapters : new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position < 0 || position >= chapters.size()) {
                return new Fragment(); // 返回空 Fragment 避免崩溃
            }
            return ArticleListFragment.newInstance(chapters.get(position).getId());
        }

        @Override
        public int getItemCount() {
            return chapters.size();
        }
    }
}
