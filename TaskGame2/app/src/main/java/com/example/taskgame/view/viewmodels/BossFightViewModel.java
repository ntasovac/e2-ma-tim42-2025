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

        bossRepository.getFirstActiveBoss(SessionManager.getInstance().getUserId(),
                new BossRepository.GetOneCallback() {
                    @Override
                    public void onSuccess(Boss boss) {
                        if (boss != null) {
                            bossLiveData.setValue(boss);
                            System.out.println("✅ First active boss: " + boss.getName() +
                                    " (Level " + boss.getLevel() + "), user id: " + boss.getUserId());
                        } else {
                            System.out.println("⚠️ No active bosses found");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.err.println("❌ Failed to fetch boss: " + e.getMessage());
                    }
                });
        /*
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
        });*/
    }

    public boolean isFightOver() {
        Boss boss = bossLiveData.getValue();
        if (boss == null) return true; // safe default
        return boss.isDefeated() || boss.getAvailableAttacks() <= 0;
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



    public void setBossPending() {
        Boss boss = bossLiveData.getValue();
        if (boss == null) {
            System.err.println("⚠️ No boss available in LiveData");
            return;
        }

        String userId = SessionManager.getInstance().getUserId();
        int bossIndex = boss.getBossIndex();

        bossRepository.setBossPending(userId, bossIndex, new BossRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                System.out.println("✅ Boss " + bossIndex + " set to PENDING");
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to set boss pending: " + e.getMessage());
            }
        });
    }

}


