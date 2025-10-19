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
    private final MutableLiveData<Boss> bossLiveData;

    public HomeViewModel() {
        welcomeText.setValue("Welcome to Home!");
        userRepository = new UserRepository();
        bossRepository = new BossRepository();
        userLiveData = userRepository.getCurrentUser();
        bossLiveData = new MutableLiveData<>();

    }

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    public void updateWelcomeText(String text) {
        welcomeText.setValue(text);
    }

    public interface BossLevelCallback {
        void onSuccess(int level);
        void onFailure(Exception e);
    }

    public void getBossLevelAsync(BossLevelCallback callback) {
        bossRepository.getFirstActiveOrPendingBoss(SessionManager.getInstance().getUserId(),
                new BossRepository.GetOneCallback() {
                    @Override
                    public void onSuccess(Boss boss) {
                        if (boss != null) {
                            bossLiveData.setValue(boss);
                            Log.d("BossLevel", "Fetched boss level: " + boss.getLevel());
                            callback.onSuccess(boss.getLevel());
                        } else {
                            Log.w("BossLevel", "No active boss found");
                            callback.onSuccess(1);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("BossLevel", "Failed to fetch boss", e);
                        callback.onFailure(e);
                    }
                });
    }
    public List<Equipment> getOwnedEquipment(){
        var user = userLiveData.getValue();
        return user.getEquipment();
    }

}