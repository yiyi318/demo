package com.example.myapplication.viewmodel.official;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.ProjectCategory;

import java.util.List;

// ProjectSystemViewModel.java
public class OfficialAccountViewModel extends ViewModel {
    private final MutableLiveData<List<ProjectCategory>> tabs = new MutableLiveData<>();
    private final Repository repository;

    public OfficialAccountViewModel(Repository repository) {
        this.repository = repository;
        loadTabs();
    }

    public LiveData<List<ProjectCategory>> getTabs() {
        return tabs;
    }

    private void loadTabs() {
        repository.getAuthorTree(new Repository.Callback<List<ProjectCategory>>() {
            @Override
            public void onSuccess(List<ProjectCategory> result) {
                tabs.postValue(result);
            }

            @Override
            public void onFailure(Throwable t) {
                // 处理错误
            }
        });
    }

}
