package com.example.myapplication.viewmodel.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.model.Article;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.network.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareViewModel extends ViewModel {
    private final Repository repository;
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private int currentPage = 1;
    private boolean hasMore = true;
    public ShareViewModel(){
        this.repository= new Repository(RetrofitClient.getService());
    }

    public ShareViewModel(Repository repository) {
        this.repository = repository;
    }

    public void refreshArticles() {
        if (isLoading.getValue() == Boolean.TRUE) return;
        currentPage = 1;
        hasMore = true;
        loadArticles();
    }

    public void loadMoreArticles() {
        Log.d("square", "loadMoreArticles: ");

        if (!hasMore || isLoading.getValue() == Boolean.TRUE) return;
        currentPage++;
        loadArticles();
    }


    public LiveData<Boolean> postShareArticle(String title,String link) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repository.postShareArticle(title,link)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(response.body().isSuccess());
                        } else {
                            result.setValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        result.setValue(false);
                    }
                });
        return result;
    }

    public LiveData<Boolean> postDeleteShareArticle(int articleId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repository.postDeleteShareArticle(articleId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(response.body().isSuccess());
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    Log.e("Network", "Error body: " + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            result.setValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        result.setValue(false);
                    }
                });
        return result;
    }



    private void loadArticles() {

        if (isLoading.getValue() == Boolean.TRUE) return; // 防抖
        isLoading.setValue(true);
        repository.getShareArticles(currentPage, new Repository.Callback<List<Article>>() {

            @Override
            public void onSuccess(List<Article> data) {
                isLoading.setValue(false);
                hasMore = !data.isEmpty();

                if (currentPage == 1) {
                    articles.setValue(data);
                } else {
                    List<Article> current = articles.getValue() != null ?
                            articles.getValue() : new ArrayList<>();
                    current.addAll(data);
                    articles.setValue(current);
                }

                Log.d("VM_STATE", "加载完成 | 页码:" + currentPage +
                        " | 是否有更多:" + hasMore + " | 数据量:" + data.size());
            }

            @Override
            public void onFailure(Throwable t) {
                isLoading.setValue(false);
                errorMsg.setValue(t.getMessage());
                if (currentPage > 1) currentPage--;
                Log.e("VM_ERROR", "加载失败: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }



    // Getters...
    public LiveData<List<Article>> getArticles() { return articles; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMsg() { return errorMsg; }
    public boolean hasMore() { return hasMore; }
}