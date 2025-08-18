package com.example.myapplication.viewmodel.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.ProjectArticleRepository;
import com.example.myapplication.model.ProjectCategory;

import java.util.List;

// ProjectSystemViewModel.java
public class ProjectSystemViewModel extends ViewModel {
    private final MutableLiveData<List<ProjectCategory>> tabs = new MutableLiveData<>();
    private final ProjectArticleRepository repository;

    public ProjectSystemViewModel(ProjectArticleRepository repository) {
        this.repository = repository;
        loadTabs();
    }

    public LiveData<List<ProjectCategory>> getTabs() {
        return tabs;
    }

    private void loadTabs() {
        repository.getProjectTabs(new ProjectArticleRepository.DataCallback<List<ProjectCategory>>() {
            @Override
            public void onSuccess(List<ProjectCategory> result) {
                tabs.postValue(result);
            }

            @Override
            public void onFailure(String error) {
                // 处理错误
            }
        });
    }

}
