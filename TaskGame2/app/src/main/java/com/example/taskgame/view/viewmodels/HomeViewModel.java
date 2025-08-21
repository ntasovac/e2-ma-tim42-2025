package com.example.taskgame.view.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> welcomeText = new MutableLiveData<>();

    public HomeViewModel() {
        welcomeText.setValue("Welcome to Home!");
    }

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    public void updateWelcomeText(String text) {
        welcomeText.setValue(text);
    }
}