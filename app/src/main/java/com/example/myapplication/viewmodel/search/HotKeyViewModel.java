package com.example.myapplication.viewmodel.search;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.HotKey;
import com.example.myapplication.network.RetrofitClient;

import java.util.List;

public class HotKeyViewModel extends ViewModel {
    private final MutableLiveData<List<HotKey>> hotKeys = new MutableLiveData<>();
    private Repository repository;
    private HotKeyViewModel(){
        repository=new Repository(RetrofitClient.getService());
    }

    public MutableLiveData<List<HotKey>> getHotKeys() {
        Log.d("VM_DEBUG", "hotKeys 类型: " + hotKeys.getClass().getSimpleName()); // 打印类型
        return hotKeys;
    }

    public void loadHotKeys() {
        repository.getHotKeys(new Repository.Callback<List<HotKey>>(){
            @Override
            public void onSuccess(List<HotKey> data) {
                hotKeys.postValue(data); // LiveData 更新
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("HotKeyVM", "加载失败", t);
            }
        });
    }

}