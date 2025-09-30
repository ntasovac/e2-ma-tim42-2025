package com.example.taskgame.view.viewmodels;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.BossRepository;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.SessionManager;

public class BossFightViewModel extends ViewModel {

    private final MutableLiveData<Boss> bossLiveData = new MutableLiveData<>();
    private final BossRepository bossRepository = new BossRepository();

    public LiveData<Boss> getBoss() {
        return bossLiveData;
    }

    public void loadBoss(String userId, int userLevel) {
        int bossIndex = userLevel - 1;
        bossRepository.getByIndex(userId, bossIndex, new BossRepository.GetOneCallback() {
            @Override
            public void onSuccess(Boss boss) {
                if (boss != null) {
                    bossLiveData.setValue(boss);
                } else {
                    // Boss ne postoji → kreiraj
                    Boss newBoss = new Boss(userId, userLevel);
                    bossRepository.create(newBoss, new BossRepository.CreateCallback() {
                        @Override
                        public void onSuccess(String id) {
                            bossLiveData.setValue(newBoss);
                        }

                        @Override
                        public void onFailure(Exception e) { }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) { }
        });
    }

    public boolean attackBoss(int damage) {
        Boss boss = bossLiveData.getValue();
        if (boss == null || !boss.canAttack()) return false;

        boolean hit = boss.takeDamage(damage);

        bossRepository.update(boss, new BossRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                bossLiveData.setValue(boss); // refresh
            }

            @Override
            public void onFailure(Exception e) { }
        });

        return hit;
    }

}
