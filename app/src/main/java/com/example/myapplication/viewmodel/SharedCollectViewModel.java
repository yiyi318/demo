package com.example.myapplication.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedCollectViewModel extends ViewModel {
    private final Repository repository;

    public SharedCollectViewModel() {
        repository = new Repository(RetrofitClient.getService());
    }

    public LiveData<Boolean> collectArticle(int articleId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repository.collectArticle(articleId).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                boolean success = response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess();
                result.setValue(success);
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    public LiveData<Boolean> uncollectArticle(int articleId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repository.uncollectArticle(articleId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }
}

