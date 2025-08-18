package com.example.myapplication.viewmodel.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.Respository.CoinRepository;

public class CoinRankViewModelFactory implements ViewModelProvider.Factory {
    private final CoinRepository repository;

    public CoinRankViewModelFactory(CoinRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CoinRankViewModel.class)) {
            return (T) new CoinRankViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}