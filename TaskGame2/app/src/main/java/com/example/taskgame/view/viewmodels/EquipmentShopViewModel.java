package com.example.taskgame.view.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.enums.EquipmentType;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class EquipmentShopViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<EquipmentType> selectedType = new MutableLiveData<>();
    private final MutableLiveData<User> userLiveData;

    public EquipmentShopViewModel() {
        userRepository = new UserRepository();
        userLiveData = getUserLiveData();
    }
    public void buyEquipment(Equipment equipment, OnCompleteListener<Object> listener) {
        userRepository.buyEquipment(equipment, listener);
    }
    public int getCurrentLevel(){
        var user = userLiveData.getValue();
        return user.getLevel();
    }
    public MutableLiveData<User> getUserLiveData(){
        return userRepository.getCurrentUser();
    }
}
