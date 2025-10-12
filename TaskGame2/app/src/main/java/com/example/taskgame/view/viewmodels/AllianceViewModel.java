package com.example.taskgame.view.viewmodels;


import android.se.omapi.Session;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.AllianceRepository;
import com.example.taskgame.data.repositories.BadgeRepository;
import com.example.taskgame.data.repositories.PotionRepository;
import com.example.taskgame.data.repositories.SpecialMissionRepository;
import com.example.taskgame.data.repositories.UserEquipmentRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Alliance;
import com.example.taskgame.domain.models.Potion;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.SpecialMission;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.domain.models.UserEquipment;

import java.util.HashMap;
import java.util.Map;

public class AllianceViewModel extends ViewModel {

    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<SpecialMission> specialMissionLiveData = new MutableLiveData<>();

    private final AllianceRepository repo = new AllianceRepository();
    private final SpecialMissionRepository missionRepo = new SpecialMissionRepository();

    public LiveData<Alliance> getAlliance() {
        return allianceLiveData;
    }

    public LiveData<SpecialMission> getSpecialMission() {
        return specialMissionLiveData;
    }

    /** 🔹 Load alliance by user ID */
    public void loadAllianceByUser(String userId) {
        repo.getByUserId(userId, new AllianceRepository.GetOneCallback() {
            @Override
            public void onSuccess(Alliance alliance) {
                allianceLiveData.setValue(alliance);

                // If alliance has an active mission, load it immediately
                if (alliance != null && alliance.isSpecialMissionActive() && alliance.getSpecialBossId() != null) {
                    loadSpecialMissionByAlliance(alliance.getName());
                }
            }

            @Override
            public void onFailure(Exception e) {
                allianceLiveData.setValue(null);
            }
        });
    }

    /** 🔹 Apply regular boss hit to special mission if active */
    public void applySpecialMissionAction(String userId, String actionType, @Nullable String difficulty) {
        Alliance alliance = allianceLiveData.getValue();
        if (alliance == null || !alliance.isSpecialMissionActive()) {
            System.out.println("Alliance vm, apply: ⚠️ No active special mission found.");
            return;
        }

        missionRepo.getByAllianceId(alliance.getId(), new SpecialMissionRepository.GetOneCallback() {
            @Override
            public void onSuccess(SpecialMission mission) {
                if (mission == null) return;

                switch (actionType) {
                    case "storePurchase":
                        mission.applyStorePurchase(userId);
                        break;
                    case "regularHit":
                        mission.applyRegularHit(userId);
                        break;
                    case "task":
                        mission.applyTask(userId, difficulty);
                        break;
                    case "otherTask":
                        mission.applyOtherTask(userId);
                        break;
                    case "noUnfinished":
                        mission.applyNoUnfinishedTasks(userId);
                        break;
                    case "dailyMessage":
                        mission.applyDailyMessage(userId);
                        break;
                }

                // ✅ Update mission in Firestore
                missionRepo.update(mission, new SpecialMissionRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        specialMissionLiveData.setValue(mission);
                        System.out.println("✅ Updated special mission after " + actionType);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.err.println("❌ Failed to update special mission after " + actionType + ": " + e.getMessage());
                    }
                });

                if(mission.isBossDefeated()){
                    rewardAllUsersFromAlliance(alliance);
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to load special mission: " + e.getMessage());
            }
        });
    }

    public void rewardAllUsersFromAlliance(Alliance alliance) {
        if (alliance == null || alliance.getMembers() == null || alliance.getMembers().isEmpty()) {
            System.out.println("⚠️ No participants found in alliance, skipping rewards.");
            return;
        }

        UserRepository userRepo = new UserRepository();
        System.out.println("🎁 Starting reward distribution for alliance: " + alliance.getId());

        for (User member : alliance.getMembers()) {
            userRepo.getUserById(member.getId().toString(), new UserRepository.GetUserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        int userLevel = user.getLevel();
                        rewardSingleUser(member.getId().toString(), userLevel);
                        System.out.println("✅ Reward given to " + member.getId() + " (Level " + userLevel + ")");
                    } else {
                        System.err.println("⚠️ User not found for ID: " + member.getId());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    System.err.println("❌ Failed to fetch user " + member.getId() + ": " + e.getMessage());
                }
            });
        }
    }



    public void rewardSingleUser(String userId, int userLevel) {
        int nextBossFullReward = (int) (200 * Math.pow(1.2, userLevel));
        UserRepository userRepo = new UserRepository();
        UserEquipmentRepository equipmentRepo = new UserEquipmentRepository();

        // ✅ 1. Give potion
        PotionRepository potionRepo = new PotionRepository();
        potionRepo.addPotion(userId, new Potion(userId, "Health Potion"), new PotionRepository.VoidCallback() {
            @Override public void onSuccess() {
                System.out.println("✅ Potion rewarded to " + userId);
            }
            @Override public void onFailure(Exception e) {
                System.err.println("❌ Failed to reward potion: " + e.getMessage());
            }
        });

        // ✅ 2. Give random equipment to the user
        String[] equipmentIds = {
                "eq_golden_crown",
                "eq_shadow_cloak",
                "eq_sword_flames",
                "eq_war_hammer"
        };

// Pick one at random
        String randomEqId = equipmentIds[(int) (Math.random() * equipmentIds.length)];

// Create and save the equipment
        UserEquipment newEquipment = new UserEquipment(userId, randomEqId, false);

        equipmentRepo.add(
                userId,
                newEquipment,
                new UserEquipmentRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("✅ Equipment " + randomEqId + " rewarded to user " + userId);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.err.println("❌ Failed to reward equipment: " + e.getMessage());
                    }
                }
        );

        // ✅ 3. Give 50% of next boss reward
        int rewardCoins = (int) (nextBossFullReward * 0.5);
        userRepo.incrementCoins(userId, rewardCoins, new UserRepository.RegisterCallback() {
            @Override
            public void onSuccess() {
                System.out.println("💰 Coins successfully rewarded to " + userId);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to reward coins: " + e.getMessage());
            }
        });


        // ✅ 4. Increment special mission badge count
        BadgeRepository badgeRepo = new BadgeRepository();

// Increment or create a badge for a user
        badgeRepo.incrementBadge(userId, "Special Mission Master", new BadgeRepository.VoidCallback() {
            @Override public void onSuccess() {
                System.out.println("🏅 Badge incremented successfully!");
            }

            @Override public void onFailure(Exception e) {
                System.err.println("❌ Failed to increment badge: " + e.getMessage());
            }
        });

    }



    /** 🔹 Load an existing special mission by ID */
    public void loadSpecialMissionByAlliance(String allianceId) {
        missionRepo.getByAllianceId(allianceId, new SpecialMissionRepository.GetOneCallback() {
            @Override
            public void onSuccess(SpecialMission mission) {
                specialMissionLiveData.setValue(mission);
                if (mission != null) {
                    System.out.println("✅ Loaded special mission for alliance: " + allianceId);
                } else {
                    System.out.println("⚠️ No special mission found for alliance vm: " + allianceId);
                }
            }

            @Override
            public void onFailure(Exception e) {
                specialMissionLiveData.setValue(null);
                System.err.println("❌ Failed to load special mission for alliance " + allianceId + ": " + e.getMessage());
            }
        });
    }


    /** 🔹 Starts or resumes a special mission */
    public void startSpecialMission(String specialBossId) {
        Alliance alliance = allianceLiveData.getValue();
        if (alliance == null) return;

        // ✅ Step 1: Check if an active mission already exists
        if (alliance.isSpecialMissionActive()) {
            // Just load existing mission and skip creation
            loadSpecialMissionByAlliance(alliance.getId());
            return;
        }

        // ✅ Step 2: Create a new mission if none active
        int membersCount = alliance.getMembers() != null ? alliance.getMembers().size() : 0;
        int totalHp = 100 * Math.max(1, membersCount);

        Map<String, Integer> userDamage = new HashMap<>();
        if (alliance.getMembers() != null) {
            for (User member : alliance.getMembers()) {
                userDamage.put(String.valueOf(member.getId()), 0);
            }
        }

        SpecialMission mission = new SpecialMission();
        mission.setAllianceId(alliance.getId());
        //mission.setParticipantIds(alliance.getMembers());
        mission.setStatus("ACTIVE");
        mission.setBossDefeated(false);
        mission.setTotalHp(totalHp);
        mission.setCurrentHp(totalHp);
        mission.setUserDamage(userDamage);
        mission.setUserActionCounts(new HashMap<>());

        missionRepo.create(alliance.getId(), mission, new SpecialMissionRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                // Update alliance
                repo.startSpecialMission(alliance.getId(), id, new AllianceRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        alliance.setSpecialMissionActive(true);
                        alliance.setSpecialBossId(id);
                        allianceLiveData.setValue(alliance);

                        specialMissionLiveData.setValue(mission);

                        System.out.println("✅ Special mission created and started!");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.err.println("❌ Failed to update alliance special mission: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to create special mission: " + e.getMessage());
            }
        });
    }
}
