package com.example.myapplication.viewmodel.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.CoinRepository;
import com.example.myapplication.model.CoinArticle;
import com.example.myapplication.model.CoinInfo;
import com.example.myapplication.model.Resource;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class CoinViewModel extends ViewModel {
    private final CoinRepository repository;
    private final MutableLiveData<CoinInfo> coinLiveData = new MutableLiveData<>();

    private final MutableLiveData<Integer> coincountLiveData = new MutableLiveData<>();

    private final MutableLiveData<Resource<List<CoinArticle>>> rankData = new MutableLiveData<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final List<CoinArticle> cachedData = new ArrayList<>();

    public CoinViewModel() {
        this.repository = new CoinRepository(RetrofitClient.getService());
    }

    public CoinViewModel(CoinRepository repository) {
        this.repository = repository;
    }

    public void loadMyCoins() {
        repository.getMyCoins().observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                coinLiveData.postValue(resource.data);
                if(resource.data!=null)
                    coincountLiveData.postValue(resource.data.getCoinCount());
            }
            // 可以在这里处理错误状态
        });
    }

    public void refreshRankList() {
        currentPage = 1;
        isLastPage = false;
        cachedData.clear();
        loadCoinList();
    }

    /**
     * 加载下一页数据
     */
    public void loadCoinList() {
        if (isLoading || isLastPage) return;

        isLoading = true;
        rankData.postValue(Resource.loading(cachedData));
        Log.d("coin", "loadCoinList: ");

        repository.getCoinArticleList(currentPage).observeForever(resource -> {
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

    public LiveData<Resource<List<CoinArticle>>> getRankData() {
        return rankData;
    }

    public LiveData<Integer> getCoinLiveData() {
        return coincountLiveData;
    }


   }
