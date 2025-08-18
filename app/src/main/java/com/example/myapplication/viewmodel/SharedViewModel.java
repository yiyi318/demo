package com.example.myapplication.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.R;
import com.example.myapplication.model.User;

import java.util.HashMap;
import java.util.Map;

public class SharedViewModel extends ViewModel {
    // 用户登录状态（私有可变的）
    private final MutableLiveData<Boolean> _userLoggedIn = new MutableLiveData<>(false);
    // 公开不可变的LiveData
    public final LiveData<Boolean> userLoggedIn = _userLoggedIn;

    // 当前选中的导航项ID
    private final MutableLiveData<Integer> _selectedNavItem = new MutableLiveData<>(-1);
    public final LiveData<Integer> selectedNavItem = _selectedNavItem;

    // 全局加载状态
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    // 用户数据
    private final MutableLiveData<User> _currentUser = new MutableLiveData<>();
    public final LiveData<User> currentUser = _currentUser;

    // 使用Map<文章ID, 收藏状态>
    private final MutableLiveData<Map<Long, Boolean>> articleFavorites = new MutableLiveData<>(new HashMap<>());

    private final MutableLiveData<Boolean> shouldShowBottomTab = new MutableLiveData<>(true);

    public LiveData<Boolean> getShouldShowBottomTab() {
        return shouldShowBottomTab;
    }

    public void setShouldShowBottomTab(boolean show) {
        Log.d("TabState", "Setting bottom tab visible: " + show);
        shouldShowBottomTab.setValue(show);
    }

    // 更新状态方法（所有Adapter共用）
    public void updateFavorite(long articleId, boolean isFavorite) {
        Map<Long, Boolean> newMap = new HashMap<>(articleFavorites.getValue());
        newMap.put(articleId, isFavorite);
        articleFavorites.postValue(newMap);
    }


    // 暴露只读LiveData给外部
    public LiveData<Map<Long, Boolean>> getFavoriteStates() {
        return articleFavorites; // 这里就是getFavoriteStates的来源
    }

    /**
     * 更新登录状态
     * @param isLoggedIn 是否已登录
     * @param user 用户数据（可选）
     *
     *
     */

    public void setLoggedIn(boolean isLoggedIn) {
        setLoggedIn(isLoggedIn, null); // 调用完整参数的方法
    }
    public void setLoggedIn(boolean isLoggedIn, User user) {
        _userLoggedIn.postValue(isLoggedIn);
        _currentUser.postValue(user);

        // 登录状态变化时自动处理
        if (isLoggedIn) {
            _selectedNavItem.postValue(R.id.nav_home); // 默认选中首页
        } else {
            _selectedNavItem.postValue(-1); // 重置选中状态
        }
    }

    /**
     * 选择导航项
     * @param itemId 菜单项ID
     */
    public void selectNavItem(int itemId) {
        if (itemId != _selectedNavItem.getValue()) {
            _selectedNavItem.postValue(itemId);
        }
    }

    /**
     * 设置加载状态
     * @param loading 是否正在加载
     */
    public void setLoading(boolean loading) {
        _isLoading.postValue(loading);
    }

    /**
     * 快速检查登录状态
     */
    public boolean isLoggedIn() {
        return Boolean.TRUE.equals(_userLoggedIn.getValue());
    }

}