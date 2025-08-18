package com.example.myapplication.viewmodel.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.CoinRepository;
import com.example.myapplication.model.CoinInfo;
import com.example.myapplication.model.Resource;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class CoinRankViewModel extends ViewModel {
    private final CoinRepository repository;
    private final MutableLiveData<Resource<List<CoinInfo>>> rankData = new MutableLiveData<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final MutableLiveData<CoinInfo> coinLiveData = new MutableLiveData<>();
    private final List<CoinInfo> cachedData = new ArrayList<>();

    // 无参构造函数（供系统使用）
    public CoinRankViewModel() {
        this.repository = new CoinRepository(RetrofitClient.getService());
    }

    // 带参构造函数（便于测试）
    public CoinRankViewModel(CoinRepository repository) {
        this.repository = repository;
    }

    /**
     * 加载第一页数据（刷新用）
     */
    public void refreshRankList() {
        currentPage = 1;
        isLastPage = false;
        cachedData.clear();
        loadRankList();
        loadMyCoins();

    }

    public void loadMyCoins() {
        repository.getMyCoins().observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                coinLiveData.postValue(resource.data);
            }
            // 可以在这里处理错误状态
        });
    }

    /**
     * 加载下一页数据
     */
    public void loadRankList() {
        if (isLoading || isLastPage) return;

        isLoading = true;
        rankData.postValue(Resource.loading(cachedData));

        repository.getCoinRankList(currentPage).observeForever(resource -> {
            isLoading = false;

            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // 成功加载数据
                if (resource.data.isEmpty()) {
                    isLastPage = true;
                } else {
                    cachedData.addAll(resource.data);
                    currentPage++;
                }
                rankData.postValue(Resource.success(new ArrayList<>(cachedData)));
            } else if (resource.status == Resource.Status.ERROR) {
                // 处理错误
                rankData.postValue(Resource.error(resource.message, cachedData));
            }
        });
    }

    public LiveData<Resource<List<CoinInfo>>> getRankData() {
        return rankData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源，防止内存泄漏
    }

    public LiveData<CoinInfo> getCoinLiveData() {
        return coinLiveData;
    }
}