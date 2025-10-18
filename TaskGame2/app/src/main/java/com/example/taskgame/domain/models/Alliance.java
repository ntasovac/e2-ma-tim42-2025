package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Alliance {
    private  String Name;
    private User Owner;
    private List<User> Members;
    private SpecialMission specialMission;
    private String Id;

    public SpecialMission getSpecialMission() {
        return specialMission;
    }

    public void setSpecialMission(SpecialMission specialMission) {
        this.specialMission = specialMission;
    }

    private boolean isMissionActive; // Whether a special mission is active
    private String specialBossId;         // ID of the special boss (if any)

    // Firestore requires an empty constructor
    public Alliance() {}

    public Alliance(String name, User owner) {
        this.Name = name;
        this.Owner = owner;
        this.Members = new ArrayList<>();
        this.isMissionActive = false; // default no mission
        this.specialMission = new SpecialMission();
        this.specialBossId = null;
    }

    // Getters

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public List<User> getMembers() {
        return Members;
    }

    public void setMembers(List<User> members) {
        Members = members;
    }

    public User getOwner() {
        return Owner;
    }

    public void setOwner(User owner) {
        Owner = owner;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isSpecialMissionActive() { return isMissionActive; }
    public String getSpecialBossId() { return specialBossId; }

    // Setters

    public void setSpecialMissionActive(boolean specialMissionActive) { this.isMissionActive = specialMissionActive; }
    public void setSpecialBossId(String specialBossId) { this.specialBossId = specialBossId; }


}
