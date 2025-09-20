package com.example.taskgame.view.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.User;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> welcomeText = new MutableLiveData<>();
    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData;

    public HomeViewModel() {
        welcomeText.setValue("Welcome to Home!");
        userRepository = new UserRepository();
        userLiveData = userRepository.getCurrentUser();
    }

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    public void updateWelcomeText(String text) {
        welcomeText.setValue(text);
    }

    public int getCurrentLevel(){
        var user = userLiveData.getValue();
        return user.getLevel();
    }
    public List<Equipment> getOwnedEquipment(){
        var user = userLiveData.getValue();
        return user.getEquipment();
    }
}