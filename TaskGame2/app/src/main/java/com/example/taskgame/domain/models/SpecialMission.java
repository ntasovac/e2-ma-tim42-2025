package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class SpecialMission {
    private String id;
    private String allianceId;
    private List<String> participantIds;
    private String status;        // ACTIVE / FINISHED
    private boolean bossDefeated;

    private double totalHp;
    private double currentHp;

    // userId -> total damage contributed
    private Map<String, Integer> userDamage;

    // userId -> action counts (e.g., "storePurchases" -> 3)
    private Map<String, Map<String, Integer>> userActionCounts;

    public SpecialMission() {}

    public SpecialMission(String id, String allianceId, List<String> participantIds, double totalHp) {
        this.id = id;
        this.allianceId = allianceId;
        this.participantIds = participantIds;
        this.totalHp = totalHp;
        this.currentHp = totalHp;
        this.status = "ACTIVE";
        this.bossDefeated = false;
        this.userDamage = new HashMap<>();
        this.userActionCounts = new HashMap<>();

        for (String userId : participantIds) {
            userDamage.put(userId, 0);
            userActionCounts.put(userId, new HashMap<>());
        }
    }

    // ✅ Core damage handler
    private void applyDamage(String userId, int damage, String action, int maxAllowed) {
        // ✅ Initialize maps if needed
        if (userDamage == null)
            userDamage = new HashMap<>();

        if (userActionCounts == null)
            userActionCounts = new HashMap<>();

        // ✅ Ensure user entries exist
        userDamage.putIfAbsent(userId, 0);
        userActionCounts.putIfAbsent(userId, new HashMap<>());

        Map<String, Integer> counts = userActionCounts.get(userId);
        if (counts == null) {
            counts = new HashMap<>();
            userActionCounts.put(userId, counts);
        }

        int currentCount = counts.getOrDefault(action, 0);

        // ✅ Stop if user already reached max actions for this type
        if (currentCount >= maxAllowed) {
            System.out.println("⚠️ User " + userId + " already reached max for " + action);
            return;
        }

        // ✅ Update action count and damage
        counts.put(action, currentCount + 1);
        userActionCounts.put(userId, counts);

        int newDamage = userDamage.get(userId) + damage;
        userDamage.put(userId, newDamage);

        // ✅ Reduce HP safely
        if (currentHp <= 0) currentHp = totalHp; // ensure initialized
        currentHp = Math.max(0, currentHp - damage);

        // ✅ Check boss status
        if (currentHp == 0) {
            bossDefeated = true;
            status = "FINISHED";
        }

        System.out.println("✅ " + userId + " dealt " + damage + " (" + action + "). Remaining HP: " + currentHp);
    }


    public int getTotalDamageDealt() {
        if (userDamage == null || userDamage.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (int dmg : userDamage.values()) {
            total += dmg;
        }
        return total;
    }


    // 🔹 Special mission actions
    public void applyStorePurchase(String userId) {
        applyDamage(userId, 2, "storePurchase", 5);
    }

    public void applyRegularHit(String userId) {
        applyDamage(userId, 2, "regularHit", 10);
    }

    public void applyTask(String userId, String difficulty) {
        if (difficulty.equalsIgnoreCase("easy_and_normal")) {
            applyDamage(userId, 2, "task", 10);
        } else {
            applyDamage(userId, 1, "task", 10);
        }
    }

    public void applyOtherTask(String userId) {
        applyDamage(userId, 4, "otherTask", 6);
    }

    public void applyNoUnfinishedTasks(String userId) {
        applyDamage(userId, 10, "noUnfinished", 1);
    }

    public void applyDailyMessage(String userId) {
        applyDamage(userId, 4, "dailyMessage", Integer.MAX_VALUE); // daily restriction enforced elsewhere
    }

    // 🔹 Progress helpers
    public int getAllianceTotalDamage() {
        return userDamage.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getProgressPercent() {
        return (totalHp - currentHp) / totalHp * 100.0;
    }

    // -------- Getters & Setters -------- //
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isBossDefeated() { return bossDefeated; }
    public void setBossDefeated(boolean bossDefeated) { this.bossDefeated = bossDefeated; }

    public double getTotalHp() { return totalHp; }
    public void setTotalHp(double totalHp) { this.totalHp = totalHp; }

    public double getCurrentHp() { return currentHp; }
    public void setCurrentHp(double currentHp) { this.currentHp = currentHp; }

    public Map<String, Integer> getUserDamage() { return userDamage; }
    public void setUserDamage(Map<String, Integer> userDamage) { this.userDamage = userDamage; }

    public Map<String, Map<String, Integer>> getUserActionCounts() { return userActionCounts; }
    public void setUserActionCounts(Map<String, Map<String, Integer>> userActionCounts) { this.userActionCounts = userActionCounts; }
}
