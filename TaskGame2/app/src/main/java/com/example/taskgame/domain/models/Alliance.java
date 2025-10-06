package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class Alliance {
    private String id;                   // Alliance ID
    private String leaderId;             // Leader of the alliance (user ID)
    private List<String> participantIds; // All participant user IDs

    private boolean specialMissionActive; // Whether a special mission is active
    private String specialBossId;         // ID of the special boss (if any)

    // Firestore requires an empty constructor
    public Alliance() {}

    public Alliance(String id, String leaderId, List<String> participantIds) {
        this.id = id;
        this.leaderId = leaderId;
        this.participantIds = participantIds;
        this.specialMissionActive = false; // default no mission
        this.specialBossId = null;
    }

    // Getters
    public String getId() { return id; }
    public String getLeaderId() { return leaderId; }
    public List<String> getParticipantIds() { return participantIds; }
    public boolean isSpecialMissionActive() { return specialMissionActive; }
    public String getSpecialBossId() { return specialBossId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public void setSpecialMissionActive(boolean specialMissionActive) { this.specialMissionActive = specialMissionActive; }
    public void setSpecialBossId(String specialBossId) { this.specialBossId = specialBossId; }

    // Utility: add participant
    public void addParticipant(String userId) {
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
        }
    }

    public int getParticipantCount() {
        return participantIds != null ? participantIds.size() : 0;
    }

    // Utility: remove participant
    public void removeParticipant(String userId) {
        participantIds.remove(userId);
    }
}
