package com.example.myapplication.viewmodel.system;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.model.Article;
import com.example.myapplication.model.Chapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemViewModel extends ViewModel {
    private final SystemRepository repository;

    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>();
    private final MutableLiveData<List<?>> emptyLiveData = new MutableLiveData<>(Collections.emptyList());
    // 一级分类数据
    private  final MutableLiveData<List<Chapter>> primaryCategories = new MutableLiveData<>();

    // 当前选中的一级分类ID
    private final MutableLiveData<Integer> selectedPrimaryCategoryId = new MutableLiveData<>();

    // 二级分类数据（根据一级分类动态加载）
    private final MutableLiveData<List<Chapter>> secondaryCategories = new MutableLiveData<>();

    // 加载状态
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // 错误信息
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final Map<String, MutableLiveData<List<Chapter>>> tabDataMap = new HashMap<>();


    public LiveData<List<Chapter>> getTabData(String tabName) {
        return tabDataMap.get(tabName);
    }

    //初始化构造函数
    public SystemViewModel(SystemRepository repository) {
        tabDataMap.put("体系", new MutableLiveData<>());
        this.repository = repository;
        loadPrimaryCategories();
    }

    // 获取一级分类
    public void loadPrimaryCategories() {
        Log.d("systemviewmodel", "loadPrimaryCategories: ");
        if (primaryCategories.getValue() != null) return; // 避免重复加载
        isLoading.setValue(true);
        repository.getPrimaryCategories(new SystemRepository.DataCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> data) {
                Log.d("ViewModel", "实际数据量: " + data.size()); // 关键检查点
                isLoading.setValue(false);
                //获取对应的primaryCategories
                primaryCategories.setValue(data);

                if (!data.isEmpty()) {
                    selectedPrimaryCategoryId.setValue(data.get(0).getId());
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }


    public LiveData<List<Article>> getArticles() {
        return articles;
    }

    public LiveData<List<?>> getEmptyLiveData() {
        return emptyLiveData;
    }

    // 提供给Fragment的LiveData
    public LiveData<List<Chapter>> getPrimaryCategories() {
        return primaryCategories;
    }

    public LiveData<List<Chapter>> getSecondaryCategories() {
        return secondaryCategories;
    }

    public LiveData<Integer> getSelectedPrimaryCategoryId() {
        return selectedPrimaryCategoryId;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }


}