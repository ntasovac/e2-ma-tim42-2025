package com.example.taskgame.view.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    private final UserRepository userRepository;

    public LoginViewModel() {
        userRepository = new UserRepository();
    }

    public void login(String email, String password) {
        userRepository.login(email, password);
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userRepository.getUserLiveData();
    }

    public LiveData<String> getErrorLiveData() {
        return userRepository.getErrorLiveData();
    }
}
