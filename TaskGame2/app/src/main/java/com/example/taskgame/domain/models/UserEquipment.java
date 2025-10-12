package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserEquipment {
    private String userId;     // Who owns this equipment
    private String equipmentId; // Reference to SpecialEquipment
    private boolean active;    // Whether it's currently active
    private long acquiredAt;   // Timestamp when it was acquired

    // Required empty constructor for Firestore
    public UserEquipment() {}

    public UserEquipment(String userId, String equipmentId, boolean active) {
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.active = active;
        this.acquiredAt = System.currentTimeMillis();
    }




    // Getters
    public String getUserId() { return userId; }
    public String getEquipmentId() { return equipmentId; }
    public boolean isActive() { return active; }
    public long getAcquiredAt() { return acquiredAt; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public void setActive(boolean active) { this.active = active; }
    public void setAcquiredAt(long acquiredAt) { this.acquiredAt = acquiredAt; }
}
