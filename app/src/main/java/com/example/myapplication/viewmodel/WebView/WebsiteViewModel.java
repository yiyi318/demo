package com.example.myapplication.viewmodel.WebView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.model.Website;
import com.example.myapplication.model.WebsiteCategory;

import java.util.List;

// NavViewModel.java
public class WebsiteViewModel extends ViewModel {
    private final SystemRepository repository;
    private final MutableLiveData<List<WebsiteCategory>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Website>> currentWebsites = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedCategoryId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public WebsiteViewModel(SystemRepository repository) {
        this.repository = repository;
    }

    // 加载分类数据
    public void loadWebsiteCategories() {
        isLoading.setValue(true);
        repository.getWebsiteCategories(new Repository.Callback<List<WebsiteCategory>>() {
            @Override
            public void onSuccess(List<WebsiteCategory> data) {
                isLoading.setValue(false);
                categories.setValue(data);
                if (data != null && !data.isEmpty()) {
                    selectCategory(data.get(0).getCid()); // 默认选中第一个分类
                }
            }

            @Override
            public void onFailure(Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    // 选择分类
    public void selectCategory(int categoryId) {
        selectedCategoryId.setValue(categoryId);
        updateCurrentWebsites(categoryId);
    }

    // 更新当前显示的网站列表
    private void updateCurrentWebsites(int categoryId) {
        List<WebsiteCategory> categoryList = categories.getValue();
        if (categoryList != null) {
            for (WebsiteCategory category : categoryList) {
                if (category.getCid() == categoryId) {
                    currentWebsites.setValue(category.getArticles());
                    break;
                }
            }
        }
    }

    // 刷新数据
    public void refresh() {
        loadWebsiteCategories();
    }

    // LiveData暴露给UI
    public LiveData<List<WebsiteCategory>> getCategories() {
        return categories;
    }

    public LiveData<List<Website>> getCurrentWebsites() {
        return currentWebsites;
    }

    public LiveData<Integer> getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}