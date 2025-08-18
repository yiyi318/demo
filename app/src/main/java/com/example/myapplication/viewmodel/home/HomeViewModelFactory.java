package com.example.myapplication.viewmodel.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.Respository.Repository;
import com.example.myapplication.Repository.BannerRepository;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final Repository repository;
    private final BannerRepository bannerRepository; // Banner数据仓库

    public HomeViewModelFactory(Repository repository,BannerRepository bannerRepository) {

        this.repository = repository;
        this.bannerRepository = bannerRepository;

    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(repository,bannerRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}