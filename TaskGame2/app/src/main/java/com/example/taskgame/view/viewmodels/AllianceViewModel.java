package com.example.taskgame.view.viewmodels;


import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.AllianceRepository;
import com.example.taskgame.data.repositories.SpecialMissionRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Alliance;
import com.example.taskgame.domain.models.Badge;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.SpecialMission;
import com.example.taskgame.domain.models.User;

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

    public void loadAlliance() {
        String allianceName = SessionManager.getInstance().getUser().getAlliance();

        if (allianceName == null || allianceName.isEmpty()) {
            Log.e("AllianceVM", "❌ User has no alliance name set in SessionManager.");
            allianceLiveData.setValue(null);
            return;
        }

        Log.d("AllianceVM", "🔍 Loading alliance: " + allianceName);
        AllianceRepository allianceRepo = new AllianceRepository();
        allianceRepo.getAllianceByName(allianceName).observeForever(alliance -> {
            if (alliance != null) {
                allianceLiveData.setValue(alliance);
                specialMissionLiveData.setValue(alliance.getSpecialMission());
                Log.d("AllianceVM", "✅ Alliance loaded successfully: " + allianceName);
            } else {
                allianceLiveData.setValue(null);
                Log.w("AllianceVM", "⚠️ Alliance not found: " + allianceName);
            }
        });
    }


    /** 🔹 Apply regular boss hit to special mission if active */
    public void applySpecialMissionAction(String userId, String actionType, @Nullable String difficulty) {
        //Alliance currentAlliance = allianceLiveData.getValue();
    /*
        if (currentAlliance == null || !currentAlliance.isSpecialMissionActive()) {
            System.out.println("⚠️ AllianceVM.apply: No active special mission found.");
            return;
        }*/

        String allianceName = SessionManager.getInstance().getUser().getAlliance();

        if (allianceName == null) {
            System.out.println("⚠️ AllianceVM.apply: No active alliance found.");
            return;
        }

        AllianceRepository allianceRepo = new AllianceRepository();

        // Step 1️⃣: Fetch the latest alliance data from Firestore
        allianceRepo.getAllianceByName(allianceName).observeForever(alliance -> {
            if (alliance == null || alliance.getSpecialMission() == null) {
                System.out.println("⚠️ AllianceVM.apply: No special mission found for alliance " + allianceName);
                return;
            }

            SpecialMission mission = alliance.getSpecialMission();

            // Step 2️⃣: Apply the action
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
                default:
                    Log.w("AllianceVM", "⚠️ Unknown actionType: " + actionType);
                    return;
            }

            // Step 3️⃣: Update the alliance's embedded mission
            allianceRepo.updateSpecialMissionByName(allianceName, mission, true, task -> {
                if (task.isSuccessful()) {
                    alliance.setSpecialMission(mission);
                    allianceLiveData.setValue(alliance);
                    specialMissionLiveData.setValue(mission);
                    System.out.println("✅ Special mission updated after action: " + actionType);
                } else {
                    System.err.println("❌ Failed to update mission after " + actionType + ": " +
                            task.getException().getMessage());
                }
            });

            // Step 4️⃣: Handle boss defeat
            if (mission.isBossDefeated()) {
                rewardAllUsersFromAlliance(alliance);
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
        int nextBossFullReward = (int) (200 * Math.pow(1.2, userLevel+1));
        UserRepository userRepo = new UserRepository();

        // ✅ 1. Grant random equipment
        userRepo.grantRandomEquipmentReward(task -> {
            if (task.isSuccessful()) {
                Equipment reward = task.getResult();
                if (reward != null) {
                    Log.d("RewardSystem", "🎁 Player won equipment: " + reward.getName());
                } else {
                    Log.d("RewardSystem", "ℹ️ No equipment dropped this time.");
                }
            } else {
                Exception e = task.getException();
                Log.e("RewardSystem", "❌ Failed to grant random equipment: " +
                        (e != null ? e.getMessage() : "unknown error"));
            }
        });

        // ✅ 2. Give 50% of next boss reward in coins
        int rewardCoins = (int) (nextBossFullReward * 0.5);
        userRepo.incrementCoins(userId, rewardCoins, new UserRepository.RegisterCallback() {
            @Override
            public void onSuccess() {
                Log.d("RewardSystem", "💰 Coins successfully rewarded to " + userId +
                        " (" + rewardCoins + " coins)");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("RewardSystem", "❌ Failed to reward coins: " + e.getMessage());
            }
        });

        // ✅ 3. Increment special mission badge count
        //BadgeRepository badgeRepo = new BadgeRepository();
        //UserRepository userRepo = new UserRepository();

        Badge newBadge = new Badge("Special Mission Master");
    userRepo.grantBadgeReward(userId, newBadge, task -> {
            if (task.isSuccessful()) {
                Log.d("RewardSystem", "🏅 Badge granted successfully for user " + userId);
            } else {
                Exception e = task.getException();
                Log.e("RewardSystem", "❌ Failed to grant badge: " + (e != null ? e.getMessage() : "unknown error"));
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


    /** 🔹 Starts or resumes a special mission
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
    }*/

    public void startSpecialMission() {

        String allianceName = SessionManager.getInstance().getUser().getAlliance();
        if (allianceName == null || allianceName.isEmpty()) {
            Log.e("AllianceVM", "❌ Alliance name is null or empty.");
            return;
        }

        AllianceRepository allianceRepo = new AllianceRepository();

        // Step 1️⃣: Fetch alliance by name
        allianceRepo.getAllianceByName(allianceName).observeForever(alliance -> {
            if (alliance == null) {
                Log.e("AllianceVM", "❌ Alliance with name '" + allianceName + "' not found.");
                return;
            }

            // Step 2️⃣: Check if mission already active
            if (Boolean.TRUE.equals(alliance.isSpecialMissionActive())) {
                Log.d("AllianceVM", "⚠️ Mission already active — loading existing mission...");
                specialMissionLiveData.setValue(alliance.getSpecialMission());
                return;
            }

            // Step 3️⃣: Create a new mission
            int membersCount = alliance.getMembers() != null ? alliance.getMembers().size() : 0;
            membersCount += 1;
            int totalHp = 100 * Math.max(1, membersCount);

            Map<String, Integer> userDamage = new HashMap<>();
            if (alliance.getMembers() != null) {
                for (User member : alliance.getMembers()) {
                    userDamage.put(String.valueOf(member.getId()), 0);
                }
            }

            SpecialMission mission = new SpecialMission();
            //mission.setAllianceId(alliance.getName());  // ✅ use alliance name as identifier
            mission.setStatus("ACTIVE");
            mission.setBossDefeated(false);
            mission.setTotalHp(totalHp);
            mission.setCurrentHp(totalHp);
            mission.setUserDamage(userDamage);
            mission.setUserActionCounts(new HashMap<>());
            //mission.setBossId(specialBossId);

            // Step 4️⃣: Create mission in Firestore (specialMissions collection)
            allianceRepo.updateSpecialMissionByName(alliance.getName(), mission, true, task -> {
                if (task.isSuccessful()) {
                    alliance.setSpecialMissionActive(true);
                    alliance.setSpecialMission(mission);
                    allianceLiveData.setValue(alliance);
                    specialMissionLiveData.setValue(mission);
                    Log.d("AllianceVM", "✅ Special mission started for alliance: " + allianceName);
                } else {
                    Log.e("AllianceVM", "❌ Failed to update alliance mission", task.getException());
                }
            });

        });
    }

}
