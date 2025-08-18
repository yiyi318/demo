package com.example.myapplication.viewmodel.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Repository.BannerRepository;
import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.Article;
import com.example.myapplication.model.Banner;

import java.util.List;



public class HomeViewModel extends ViewModel {
    // 数据仓库
    private final Repository repository;
    // Banner数据仓库
    private final BannerRepository bannerRepository;  // Banner数据仓库
    //article数据相关
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>();
    // 加载状态 article LiveData
    private final MutableLiveData<Boolean> isLoadingArticles = new MutableLiveData<>(false);

    // Banner数据相关LiveData
    private final MutableLiveData<List<Banner>> banners = new MutableLiveData<>();
    //加载状态banner LiveData
    private final MutableLiveData<Boolean> isLoadingBanners = new MutableLiveData<>(false);

    // 加载状态 错误信息 LiveData
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    // 页码控制
    private int currentPage = 0;
    //新增分页逻辑
    //是否还有更新页
    private final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(true);
    //是否全部加载完成
    private final MutableLiveData<Boolean> isLoadingCombined = new MutableLiveData<>();
    //暴露文章接口
    public LiveData<List<Article>> getArticles() {
        return articles;
    }

//暴露banner接口
    public LiveData<List<Banner>> getBanners() {
        return banners;
    }
    //暴露错误信息接口
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
//构造函数
    public HomeViewModel(Repository articleRepository, BannerRepository bannerRepository) {
        this.repository = articleRepository;
        this.bannerRepository = bannerRepository;
    }
//暴露合并加载状态
    public LiveData<Boolean> getIsLoadingCombined() {
        return isLoadingCombined;
    }

//暴露加载更多接口
    public LiveData<Boolean> getisLoadingArticles(){

        return isLoadingArticles;
    }

    public void refreshAll() {
        // 1. 检查是否已在刷新中（避免重复刷新）
        if (Boolean.TRUE.equals(isLoadingCombined.getValue())) return;

        // 重置状态
        currentPage = 0;
        isLoadingCombined.setValue(true);//// 表示当前是“加载中”状态
        errorMessage.setValue(null);
        // 同时发起两个真实请求
        loadBanners();
        loadArticles(currentPage);
    }


    // 合并加载状态
    private void updateCombinedState() {
        boolean combinedLoading =
                Boolean.TRUE.equals(isLoadingArticles.getValue()) &&
                        Boolean.TRUE.equals(isLoadingBanners.getValue());
        isLoadingCombined.setValue(combinedLoading);
    }


    // 加载初始数据 包括文章和banner
    public void loadInitialArticles() {
        currentPage = 0;
        loadArticles(currentPage);
    }

    // 加载更多数据（分页）
    public void loadMoreArticles() {
        // 1. 检查是否可加载
        if (!Boolean.TRUE.equals(hasMore.getValue()) || Boolean.TRUE.equals(isLoadingCombined.getValue())) {
            return;
        }
        currentPage++;
        loadArticles(currentPage);
    }

    //加载文章 只有初始化的时候会调用这个方法
    private void loadArticles(int page) {
        isLoadingArticles.setValue(true);//正在加载中
        errorMessage.setValue(null);//只有在更新失败才会被赋值
        updateCombinedState(); // 添加这行
        repository.getArticles(page, new Repository.Callback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> data) {
                isLoadingArticles.setValue(false);//这里是加载完成
                hasMore.setValue(!data.isEmpty());
                updateCombinedState(); // 添加这行
                if (page == 0) {
                    // 首次加载，直接设置数据
                    articles.setValue(data);
                } else {
                    // 分页加载，合并数据
                    List<Article> oldData = articles.getValue();
                    if (oldData != null) {
                        oldData.addAll(data);
                        articles.setValue(oldData);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                isLoadingArticles.setValue(false);
                updateCombinedState(); // 添加这行
                errorMessage.setValue("加载失败：" + t.getMessage());
                // 分页加载失败时，回退页码
                if (page > 0) {
                    currentPage--;
                }
            }
        });
    }



    // Banner数据加载
    public void loadBanners() {
        isLoadingBanners.setValue(true);//正在加载中banner
        updateCombinedState(); // 添加这行
        bannerRepository.getBanners(new BannerRepository.Callback<List<Banner>>() {
            @Override
            public void onSuccess(List<Banner> data) {
                isLoadingBanners.setValue(false);//加载结束
                updateCombinedState(); // 添加这行
                banners.setValue(data);//更新数据 banner也被监视着
            }

            @Override
            public void onFailure(Throwable t) {
                isLoadingBanners.setValue(false);//加载结束
                updateCombinedState(); // 添加这行
                errorMessage.setValue("Banner加载失败: " + t.getMessage());
            }
        });
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null); // 在 ViewModel 内部可以调用 setValue()
    }

}
