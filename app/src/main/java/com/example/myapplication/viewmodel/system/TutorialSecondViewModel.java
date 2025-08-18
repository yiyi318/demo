package com.example.myapplication.viewmodel.system;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.Respository.SystemRepository;
import com.example.myapplication.model.TutorialChapter;
import com.example.myapplication.network.RetrofitClient;

import java.util.List;

public class TutorialSecondViewModel extends ViewModel {
    // 用于存储章节数据的LiveData
    private final MutableLiveData<List<TutorialChapter>> chaptersLiveData = new MutableLiveData<>();
    // 用于存储错误信息的LiveData
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    // 用于标识加载状态的LiveData
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    // 仓库实例（假设已通过依赖注入或直接初始化）
    private final SystemRepository repository;

    public TutorialSecondViewModel() {
        this.repository = new SystemRepository(RetrofitClient.getService());
    }
    // 带参构造函数（
    public TutorialSecondViewModel(SystemRepository repository) {
        this.repository = repository;
    }

    // 获取章节数据的LiveData（供UI观察）
    public MutableLiveData<List<TutorialChapter>> getChaptersLiveData() {
        return chaptersLiveData;
    }

    // 获取错误信息的LiveData
    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    // 获取加载状态的LiveData
    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    /**
     * 加载教程章节数据
     * @param courseId 课程ID
     */
    public void loadTutorialChapters(int courseId) {
        // 通知UI开始加载
        loadingLiveData.setValue(true);

        // 调用仓库方法获取数据
        repository.getTutorialChapters(courseId, new Repository.Callback<List<TutorialChapter>>() {
            @Override
            public void onSuccess(List<TutorialChapter> data) {
                // 加载完成，更新数据并通知UI
                loadingLiveData.setValue(false);
                chaptersLiveData.setValue(data);
            }

            @Override
            public void onFailure(Throwable t) {
                // 加载失败，更新错误信息并通知UI
                loadingLiveData.setValue(false);
                errorLiveData.setValue(t.getMessage() != null ? t.getMessage() : "获取章节失败");
            }
        });
    }
}