package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Alliance {
    private  String Name;
    private User Owner;
    private List<User> Members;
    private String Id;
    private boolean specialMissionActive; // Whether a special mission is active
    private String specialBossId;         // ID of the special boss (if any)

    // Firestore requires an empty constructor
    public Alliance() {}

    public Alliance(String name, User owner) {
        this.Name = name;
        this.Owner = owner;
        this.Members = new ArrayList<>();
        this.specialMissionActive = false; // default no mission
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

    public boolean isSpecialMissionActive() { return specialMissionActive; }
    public String getSpecialBossId() { return specialBossId; }

    // Setters

    public void setSpecialMissionActive(boolean specialMissionActive) { this.specialMissionActive = specialMissionActive; }
    public void setSpecialBossId(String specialBossId) { this.specialBossId = specialBossId; }


}
