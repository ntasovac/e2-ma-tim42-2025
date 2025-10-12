package com.example.taskgame.view.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.BossRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.User;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> welcomeText = new MutableLiveData<>();
    private final UserRepository userRepository;
    private final BossRepository bossRepository;
    private final MutableLiveData<User> userLiveData;
    //private final MutableLiveData<Boss> bossLiveData;

    public HomeViewModel() {
        welcomeText.setValue("Welcome to Home!");
        userRepository = new UserRepository();
        bossRepository = new BossRepository();
        userLiveData = userRepository.getCurrentUser();
        //bossLiveData = bossRepository.getByIndex();
    }

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    public void updateWelcomeText(String text) {
        welcomeText.setValue(text);
    }

    /*public int getBossLevel(){
        var boss = bossLiveData.getValue();
        return boss.getLevel();
    }*/
    public List<Equipment> getOwnedEquipment(){
        var user = userLiveData.getValue();
        return user.getEquipment();
    }

}