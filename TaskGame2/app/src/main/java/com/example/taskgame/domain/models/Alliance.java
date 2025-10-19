package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Alliance {
    private  String Name;
    private User Owner;
    private List<User> Members;
    private SpecialMission specialMission;
    private List<SpecialMission> allSpecialMissions;
    private List<SpecialMission> doneSpecialMissions;

    private String Id;

    public List<SpecialMission> getDoneSpecialMissions() {
        return doneSpecialMissions;
    }

    public void setDoneSpecialMissions(List<SpecialMission> doneSpecialMissions) {
        this.doneSpecialMissions = doneSpecialMissions;
    }

    public List<SpecialMission> getAllSpecialMissions() {
        return allSpecialMissions;
    }

    public void setAllSpecialMissions(List<SpecialMission> allSpecialMissions) {
        this.allSpecialMissions = allSpecialMissions;
    }



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
    public List<User> getAllMembers() {
        List<User> all = new ArrayList<>(Members);
        all.add(Owner);
        return all;
    }

    public void setMembers(List<User> members) {
        Members = members;
    }

    public User getOwner() {
        return Owner;
    }
    public void addSpecialMission(SpecialMission mission) {
        if (allSpecialMissions == null) {
            allSpecialMissions = new ArrayList<>();
        }

        allSpecialMissions.add(mission);
    }

    public void addDoneSpecialMission(SpecialMission mission) {
        if (doneSpecialMissions == null) {
            doneSpecialMissions = new ArrayList<>();
        }

        doneSpecialMissions.add(mission);
    }

    @PropertyName("isMissionActive")
    public boolean isMissionActive() {
        return isMissionActive;
    }

    @PropertyName("isMissionActive")
    public void setMissionActive(boolean missionActive) {
        this.isMissionActive = missionActive;
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
